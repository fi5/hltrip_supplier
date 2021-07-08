package com.huoli.trip.supplier.web.controller;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.huoli.trip.common.vo.response.BaseResponse;
import com.huoli.trip.supplier.api.DynamicProductItemService;
import com.huoli.trip.supplier.web.service.CommonService;
import com.huoli.trip.supplier.web.task.RefreshItemTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 描述：<br/>
 * 版权：Copyright (c) 2011-2020<br>
 * 公司：活力天汇<br>
 * 作者：冯志强<br>
 * 版本：1.0<br>
 * 创建日期：2020/8/31<br>
 */
@RestController
@RequestMapping(value = "/common", produces = "application/json")
@Slf4j
public class CommonController {

    @Autowired
    private RefreshItemTask refreshItemTask;

    @Autowired
    private DynamicProductItemService dynamicProductItemService;

    @Autowired
    private CommonService commonService;

    @PostMapping("/refresh/item/all")
    public BaseResponse refreshItemAll(@RequestParam @NotBlank(message = "user不能为空") String user){
        try {
            log.info("开始刷新item。。word={}", user);
            refreshItemTask.refreshItemProduct();
        } catch (Exception e) {
            log.error("刷新item异常，word={}", user, e);
            return BaseResponse.withFail(-1, "刷新item失败");
        }
        return BaseResponse.withSuccess();
    }

    @PostMapping("/refresh/item/code")
    public BaseResponse refreshItemByCode(@RequestParam @NotBlank(message = "user不能为空") String user, @RequestParam @NotBlank(message = "code不能为空") List<String> code){
        try {
            log.info("开始刷新item。。word={}, code={}", user, JSON.toJSONString(code));
            dynamicProductItemService.refreshItemByCode(code);
        } catch (Exception e) {
            log.error("刷新item异常，word={}, code={}", user, code, e);
            return BaseResponse.withFail(-1, "刷新item失败");
        }
        return BaseResponse.withSuccess();
    }

    @PostMapping("/refresh/item/productcode")
    public BaseResponse refreshItemByProductCode(@RequestParam @NotBlank(message = "user不能为空") String user, @RequestParam @NotNull(message = "productCode不能为空") List<String> productCode){
        try {
            log.info("开始刷新item。。word={}, productCode={}", user, JSON.toJSONString(productCode));
            dynamicProductItemService.refreshItemByProductCode(productCode);
        } catch (Exception e) {
            log.error("刷新item异常，word={}, productCode={}", user, JSON.toJSONString(productCode), e);
            return BaseResponse.withFail(-1, "刷新item失败");
        }
        return BaseResponse.withSuccess();
    }

    @PostMapping("/trans/tours")
    public BaseResponse transTours(){
        try {
            log.info("开始转移录入后台数据。");
            commonService.transTours();
        } catch (Exception e) {
            log.error("转移录入后台数据异常", e);
            return BaseResponse.withFail(-1, "刷新item失败");
        }
        return BaseResponse.withSuccess();
    }

    @PostMapping("/trans/scenic")
    public BaseResponse transScenic(){
        try {
            log.info("开始转移录入后台数据。");
            commonService.transScenic();
        } catch (Exception e) {
            log.error("转移录入后台数据异常", e);
            return BaseResponse.withFail(-1, "刷新item失败");
        }
        return BaseResponse.withSuccess();
    }

    @PostMapping("/trans/scenic/code")
    public BaseResponse transScenic(@RequestBody List<String> codes){
        try {
            log.info("开始转移景点,根据code。");
            commonService.transScenic(codes);
        } catch (Exception e) {
            log.error("转移景点,根据code，异常", e);
            return BaseResponse.withFail(-1, "刷新item失败");
        }
        return BaseResponse.withSuccess();
    }
}
