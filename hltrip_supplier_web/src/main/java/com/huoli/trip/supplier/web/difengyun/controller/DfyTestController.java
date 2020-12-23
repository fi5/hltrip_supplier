package com.huoli.trip.supplier.web.difengyun.controller;

import com.huoli.trip.common.vo.response.BaseResponse;
import com.huoli.trip.supplier.api.DfyOrderService;
import com.huoli.trip.supplier.self.difengyun.DfyOrderDetail;
import com.huoli.trip.supplier.self.difengyun.vo.request.DfyBaseRequest;
import com.huoli.trip.supplier.self.difengyun.vo.request.DfyProductNoticeRequest;
import com.huoli.trip.supplier.self.difengyun.vo.request.DfyScenicListRequest;
import com.huoli.trip.supplier.self.difengyun.vo.response.DfyBaseResult;
import com.huoli.trip.supplier.self.yaochufa.vo.BaseOrderRequest;
import com.huoli.trip.supplier.web.difengyun.service.DfySyncService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 描述：<br/>
 * 版权：Copyright (c) 2011-2020<br>
 * 公司：活力天汇<br>
 * 作者：冯志强<br>
 * 版本：1.0<br>
 * 创建日期：2020/12/22<br>
 */
@RestController
@Slf4j
@RequestMapping(value = "/dfy/test")
public class DfyTestController {

    @Autowired
    private DfySyncService dfySyncService;
    @Autowired
    DfyOrderService dfyOrderService;


    /**
     * 接收产品更新通知
     * @param productId
     * @return
     */
    @PostMapping(path = "/sync/product")
    DfyBaseResult syncProduct(@RequestBody String productId) {
        dfySyncService.syncProduct(productId, null);
        return DfyBaseResult.success();
    }

    /**
     * 接收产品更新通知
     * @param request
     * @return
     */
    @PostMapping(path = "/sync/scenic")
    DfyBaseResult syncScenic(@RequestBody DfyScenicListRequest request) {
        dfySyncService.syncScenicList(request);
        return DfyBaseResult.success();
    }

    /**
     * 订单详情
     * @param request
     * @return
     */
    @PostMapping(path = "/order/detail")
    DfyBaseResult orderDetail(@RequestBody BaseOrderRequest request) {
        final BaseResponse<DfyOrderDetail> dfyOrderDetailBaseResponse = dfyOrderService.orderDetail(request);
        return DfyBaseResult.success(dfyOrderDetailBaseResponse);
    }
}
