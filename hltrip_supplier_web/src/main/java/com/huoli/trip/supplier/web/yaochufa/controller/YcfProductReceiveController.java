package com.huoli.trip.supplier.web.yaochufa.controller;

import com.alibaba.fastjson.JSON;
import com.huoli.trip.supplier.api.DynamicProductItemService;
import com.huoli.trip.supplier.api.YcfSyncService;
import com.huoli.trip.supplier.self.yaochufa.constant.YcfConstants;
import com.huoli.trip.supplier.self.yaochufa.vo.YcfGetPriceRequest;
import com.huoli.trip.supplier.self.yaochufa.vo.YcfPrice;
import com.huoli.trip.supplier.self.yaochufa.vo.YcfProduct;
import com.huoli.trip.supplier.self.yaochufa.vo.YcfPushProductResponse;
import com.huoli.trip.supplier.self.yaochufa.vo.basevo.YcfBaseResult;
import com.huoli.trip.supplier.web.yaochufa.task.SyncPriceTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 描述：<br/>
 * 版权：Copyright (c) 2011-2020<br>
 * 公司：活力天汇<br>
 * 作者：冯志强<br>
 * 版本：1.0<br>
 * 创建日期：2020/6/24<br>
 */
@RestController()
@RequestMapping(value = "/product", produces = "application/json")
@Slf4j
public class YcfProductReceiveController {

    @Autowired
    private YcfSyncService ycfSyncService;

    @Autowired
    private DynamicProductItemService dynamicProductItemService;

    @Autowired
    private SyncPriceTask syncPriceTask;

    @PostMapping("/receive/product")
    public YcfBaseResult<YcfPushProductResponse> receiveProduct(@RequestBody List<YcfProduct> products){
        try {
            log.info("开始接收产品。。{}", JSON.toJSONString(products));
//            ycfSyncService.syncProduct(products);
            ycfSyncService.syncScenicProduct(products);
        } catch (Exception e) {
            log.error("接收产品推送失败，", e);
            return YcfBaseResult.fail(new YcfPushProductResponse(YcfConstants.PRODUCT_HANDLE_STATUS_FAIL, YcfConstants.PRODUCT_STATUS_VALID, YcfConstants.HANDLE_TYPE_ASYNC));
        }
        return YcfBaseResult.success(new YcfPushProductResponse(YcfConstants.PRODUCT_HANDLE_STATUS_SUCCESS, YcfConstants.PRODUCT_STATUS_VALID, YcfConstants.HANDLE_TYPE_ASYNC));
    }

    @PostMapping("/receive/price")
    public YcfBaseResult<YcfPushProductResponse> receivePrice(@RequestBody YcfPrice price){
        try {
            log.info("开始接收价格。。{}", JSON.toJSONString(price));
//            ycfSyncService.syncPrice(price);
            ycfSyncService.syncPriceV2(price);
        } catch (Exception e) {
            return YcfBaseResult.fail();
        }
        return YcfBaseResult.success();
    }

    @PostMapping("/sync/price")
    public YcfBaseResult syncPrice(@RequestBody YcfGetPriceRequest request){
        try {
            log.info("开始手动同步价格。。{}", JSON.toJSONString(request));
            ycfSyncService.getPrice(request);
        } catch (Exception e) {
            log.error("手动同步价格失败", e);
            return YcfBaseResult.fail();
        }
        return YcfBaseResult.success();
    }

    @PostMapping("/sync/full/price")
    public YcfBaseResult syncFullPrice(){
        try {
            log.info("开始手动同步全量价格。。");
            syncPriceTask.syncFullPrice();
        } catch (Exception e) {
            log.error("手动同步全量价格失败", e);
            return YcfBaseResult.fail();
        }
        return YcfBaseResult.success();
    }
}
