package com.huoli.trip.supplier.web.controller;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.huoli.trip.common.vo.response.BaseResponse;
import com.huoli.trip.supplier.api.DynamicProductItemService;
import com.huoli.trip.supplier.web.service.CommonService;
import com.huoli.trip.supplier.web.task.RefreshItemTask;
import com.huoli.trip.supplier.web.task.UploadImageToLocalTask;
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
    public BaseResponse transTours(@RequestParam String code){
        try {
            log.info("开始转移录入后台数据。");
            commonService.transTours(code);
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

    @PostMapping("/refresh/product/status")
    public BaseResponse refreshProductStatus(@RequestParam String productCode){
        try {
            log.info("开始刷新产品状态。。code={}", productCode);
            commonService.checkProductReverse(productCode);
        } catch (Exception e) {
            log.error("刷新产品状态异常，code={}", productCode, e);
            return BaseResponse.withFail(-1, "刷新产品状态失败");
        }
        return BaseResponse.withSuccess();
    }

    @PostMapping("/refresh/product/status/item")
    public BaseResponse refreshProductStatusByItemId(@RequestParam String itemCode){
        try {
            log.info("开始刷新产品状态。。itemCode={}", itemCode);
            commonService.checkProductReverseByItemId(itemCode);
        } catch (Exception e) {
            log.error("刷新产品状态异常，itemCode={}", itemCode, e);
            return BaseResponse.withFail(-1, "刷新产品状态失败");
        }
        return BaseResponse.withSuccess();
    }

    @PostMapping("/refresh/poi/review/city")
    public BaseResponse refreshProductStatusByItemId(){
        try {
            log.info("开始刷新审核列表城市。。");
            commonService.setPoiReviewCity();
        } catch (Exception e) {
            log.error("刷新审核列表城市异常", e);
            return BaseResponse.withFail(-1, "刷新审核列表城市失败");
        }
        return BaseResponse.withSuccess();
    }

    @PostMapping("/refresh/upload/image")
    public BaseResponse refreshUploadImageTolocal(@RequestBody List<String> ids){
        try {
            log.info("开始刷新图片数据");
            commonService.upLoadImageToLocal(ids);
        } catch (Exception e) {
            log.error("刷新图片数据异常", e);
            return BaseResponse.withFail(-1, "刷新图片数据失败");
        }
        return BaseResponse.withSuccess();
    }

    @PostMapping("/refresh/scenic/detail")
    public BaseResponse refreshScenicSpotDetailDesc(@RequestBody List<String> ids){
        try {
            log.info("开始更新景点描述");
            commonService.refreshScenicSpotDetailDesc(ids);
        } catch (Exception e) {
            log.error("更新景点描述异常", e);
            return BaseResponse.withFail(-1, "更新景点描述失败");
        }
        return BaseResponse.withSuccess();
    }

    @PostMapping("/clean/passengerTemplate")
    public BaseResponse cleanPassengerTemplate(@RequestParam("channel") String channel ){
        try {
            log.info("开始清除重复出行人模板");
            commonService.cleanPsTmp(channel);
        } catch (Exception e) {
            log.error("清除重复出行人模板异常", e);
            return BaseResponse.withFail(-1, "清除重复出行人模板失败");
        }
        return BaseResponse.withSuccess();
    }
}
