package com.huoli.trip.supplier.web.difengyun.controller;

import com.huoli.trip.common.entity.TripRefundNotify;
import com.huoli.trip.common.util.DateTimeUtil;
import com.huoli.trip.common.vo.response.BaseResponse;
import com.huoli.trip.supplier.api.DfyOrderService;
import com.huoli.trip.supplier.self.difengyun.DfyOrderDetail;
import com.huoli.trip.supplier.self.difengyun.constant.DfyConstants;
import com.huoli.trip.supplier.self.difengyun.vo.request.*;
import com.huoli.trip.supplier.self.difengyun.vo.response.DfyBaseResult;
import com.huoli.trip.supplier.self.difengyun.vo.response.DfyBillResponse;
import com.huoli.trip.supplier.self.yaochufa.vo.BaseOrderRequest;
import com.huoli.trip.supplier.web.difengyun.service.DfySyncService;
import com.huoli.trip.supplier.web.difengyun.task.DfySyncTask;
import com.huoli.trip.supplier.web.mapper.TripOrderRefundMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;

import static com.huoli.trip.supplier.self.common.SupplierConstants.PRODUCT_SYNC_MODE_UNLIMITED;

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

    @Autowired
    TripOrderRefundMapper tripOrderRefundMapper;

    @Autowired
    private DfySyncTask  dfySyncTask;


    /**
     * 接收产品更新通知
     *
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
     *
     * @param request
     * @return
     */
    @PostMapping(path = "/sync/scenic")
    DfyBaseResult syncScenic(@RequestBody DfyScenicListRequest request) {
        dfySyncService.syncScenicList(request);
        return DfyBaseResult.success();
    }

    /**
     * 更新单个景点
     *
     * @param ticketId
     * @return
     */
    @PostMapping(path = "/sync/scenic/detail")
    DfyBaseResult syncScenicDetail(@RequestBody String ticketId) {
        dfySyncService.syncScenicDetail(ticketId);
        return DfyBaseResult.success();
    }

    /**
     * 更新新产品
     *
     * @return
     */
    @PostMapping(path = "/sync/new/product")
    DfyBaseResult syncNewProduct() {
        dfySyncTask.syncNewProduct();
        return DfyBaseResult.success();
    }

    /**
     * 更新已有产品
     *
     * @return
     */
    @PostMapping(path = "/sync/update/product")
    DfyBaseResult syncUpdateProduct() {
        dfySyncTask.syncUpdateProduct();
        return DfyBaseResult.success();
    }


    /**
     * 接收产品更新通知
     *
     * @param productId
     * @return
     */
    @PostMapping(path = "/sync/product/v2")
    DfyBaseResult syncProductV2(@RequestBody String productId) {
        dfySyncService.syncProductV2(productId);
        return DfyBaseResult.success();
    }

    /**
     * 同步景点
     *
     * @param request
     * @return
     */
    @PostMapping(path = "/sync/scenic/v2")
    DfyBaseResult syncScenicV2(@RequestBody DfyScenicListRequest request) {
        dfySyncService.syncScenicListV2(request);
        return DfyBaseResult.success();
    }

    /**
     * 更新单个景点
     *
     * @param ticketId
     * @return
     */
    @PostMapping(path = "/sync/scenic/detail/v2")
    DfyBaseResult syncScenicDetailV2(@RequestBody String ticketId) {
        dfySyncService.syncScenicDetailV2(ticketId);
        return DfyBaseResult.success();
    }

    /**
     * 接收产品更新通知
     *
     * @return
     */
    @PostMapping(path = "/sync/new/product/v2")
    DfyBaseResult syncNewProductV2() {
        dfySyncTask.syncNewProductV2();
        return DfyBaseResult.success();
    }

    /**
     * 接收产品更新通知
     *
     * @return
     */
    @PostMapping(path = "/sync/update/product/v2")
    DfyBaseResult syncUpdateProductV2() {
        dfySyncTask.syncUpdateProductV2();
        return DfyBaseResult.success();
    }

    @PostMapping(path = "/sync/update/product/id/v2")
    DfyBaseResult syncUpdateProductByIdV2(@RequestBody String productId) {
        dfySyncService.syncProductV2(productId);
        return DfyBaseResult.success();
    }

    @PostMapping(path = "/sync/tours/list")
    DfyBaseResult syncToursList(@RequestBody DfyToursListRequest request) {
        return DfyBaseResult.success(dfySyncService.syncToursList(request));
    }

    @PostMapping(path = "/sync/tours/detail")
    DfyBaseResult syncToursDetail(@RequestBody String productId) {
        dfySyncService.syncToursDetail(productId, PRODUCT_SYNC_MODE_UNLIMITED);
        return DfyBaseResult.success();
    }

    @PostMapping(path = "/sync/tours/multi/detail")
    DfyBaseResult syncToursMultiDetail(@RequestBody String productId) {
        dfySyncService.syncToursDetail(productId, PRODUCT_SYNC_MODE_UNLIMITED);
        return DfyBaseResult.success();
    }

    @PostMapping(path = "/sync/tours/price")
    DfyBaseResult syncToursPrice(@RequestBody DfyToursCalendarRequest request) {
        dfySyncService.syncToursPrice(String.valueOf(request.getProductId()), String.valueOf(request.getDepartCityCode()));
        return DfyBaseResult.success();
    }

    @PostMapping(path = "/sync/tours/list/v2")
    DfyBaseResult syncToursListV2(@RequestBody DfyToursListRequest request) {
        return DfyBaseResult.success(dfySyncService.syncToursListV2(request));
    }

    @PostMapping(path = "/sync/tours/detail/v2")
    DfyBaseResult syncToursDetailV2(@RequestBody String productId) {
        dfySyncService.syncToursDetailV2(productId);
        return DfyBaseResult.success();
    }

    @PostMapping(path = "/sync/tours/price/v2")
    DfyBaseResult syncToursPriceV2(@RequestBody DfyToursCalendarRequest request) {
        dfySyncService.syncToursPriceV2(String.valueOf(request.getProductId()), String.valueOf(request.getDepartCityCode()));
        return DfyBaseResult.success();
    }

    @PostMapping(path = "/sync/tours/update")
    DfyBaseResult syncToursUpdate() {
        dfySyncTask.syncUpdateToursProduct();
        return DfyBaseResult.success();
    }

    @PostMapping(path = "/sync/tours/new")
    DfyBaseResult syncToursNew() {
        dfySyncTask.syncNewToursProduct();
        return DfyBaseResult.success();
    }


    /**
     * 订单详情
     *
     * @param request
     * @return
     */
    @PostMapping(path = "/order/detail")
    DfyBaseResult orderDetail(@RequestBody BaseOrderRequest request) {
        final BaseResponse<DfyOrderDetail> dfyOrderDetailBaseResponse = dfyOrderService.orderDetail(request);
        return DfyBaseResult.success(dfyOrderDetailBaseResponse);
    }

    /**
     * 订单详情
     *
     * @param request
     * @return
     */
    @PostMapping(path = "/order/bill")
    DfyBaseResult queryBill(int status) {
        try {
            DfyBillQueryDataReq billQueryDataReq = new DfyBillQueryDataReq();
            billQueryDataReq.setAccType(1);
            billQueryDataReq.setBillType(2);
            billQueryDataReq.setStart(0);
            billQueryDataReq.setLimit(50);
            Date createDate = new Date();
            billQueryDataReq.setStatus(status);

            billQueryDataReq.setBeginTime(DateTimeUtil.format(DateTimeUtil.addDay(createDate, -1), DateTimeUtil.YYYYMMDDHHmmss));
            billQueryDataReq.setEndTime(DateTimeUtil.format(DateTimeUtil.addDay(createDate, 10), DateTimeUtil.YYYYMMDDHHmmss));
            DfyBaseResult<DfyBillResponse> dfyBillResponseDfyBaseResult = dfyOrderService.queryBill(billQueryDataReq);
            return DfyBaseResult.success(dfyBillResponseDfyBaseResult);
        } catch (Exception e) {
            log.error("信息{}", e);
            return null;
        }
    }
    @PostMapping(path = "/order/doJob")
    DfyBaseResult testJob(){
        try {
            List<TripRefundNotify> pendingNotifys = tripOrderRefundMapper.getPendingNotifys();
            pendingNotifys.forEach(item -> {
                try {
                    dfyOrderService.processNotify(item);
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    log.error("处理退款通知失败", e);
                } catch (Exception e) {
                    log.error("处理退款通知失败了，id={}", item.getId(), e);
                }
            });
        } catch (Exception e) {
        	log.error("信息{}",e);
        }
        return null;
    }

}
