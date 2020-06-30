package com.huoli.trip.supplier.web.yaochufa.controller;

import com.huoli.trip.supplier.self.yaochufa.constant.YcfConstants;
import com.huoli.trip.supplier.self.yaochufa.vo.YcfPrice;
import com.huoli.trip.supplier.self.yaochufa.vo.YcfProduct;
import com.huoli.trip.supplier.self.yaochufa.vo.YcfPushProductResponse;
import com.huoli.trip.supplier.self.yaochufa.vo.basevo.YcfBaseResult;
import com.huoli.trip.supplier.web.yaochufa.service.YcfSyncService;
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

    @PostMapping("/receive/product")
    public YcfBaseResult<YcfPushProductResponse> receiveProduct(@RequestBody YcfProduct product){
        try {
            ycfSyncService.syncProduct(product);
        } catch (Exception e) {
            log.error("接收产品推送失败，", e);
            return YcfBaseResult.fail(new YcfPushProductResponse(YcfConstants.PRODUCT_HANDLE_STATUS_FAIL, YcfConstants.PRODUCT_STATUS_VALID, YcfConstants.HANDLE_TYPE_ASYNC));
        }
        return YcfBaseResult.success(new YcfPushProductResponse(YcfConstants.PRODUCT_HANDLE_STATUS_SUCCESS, YcfConstants.PRODUCT_STATUS_VALID, YcfConstants.HANDLE_TYPE_ASYNC));
    }

    @PostMapping("/receive/price")
    public YcfBaseResult<YcfPushProductResponse> receivePrice(@RequestBody YcfPrice price){
        try {
            ycfSyncService.syncPrice(price);
        } catch (Exception e) {
            return YcfBaseResult.fail();
        }
        return YcfBaseResult.success();
    }

    @PostMapping(value = "/test/getpoi")
    public String test(@RequestBody List<String> ids){
        ycfSyncService.syncProductItem(ids);
        return "ok";
    }
}
