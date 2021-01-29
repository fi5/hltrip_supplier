package com.huoli.trip.supplier.feign.client.difengyun.client.impl;

import com.huoli.trip.supplier.feign.client.difengyun.client.IDiFengYunClient;
import com.huoli.trip.supplier.self.difengyun.DfyOrderDetail;
import com.huoli.trip.supplier.self.difengyun.vo.DfyScenicDetail;
import com.huoli.trip.supplier.self.difengyun.vo.DfyTicketDetail;
import com.huoli.trip.supplier.self.difengyun.vo.DfyToursOrderDetail;
import com.huoli.trip.supplier.self.difengyun.vo.request.*;
import com.huoli.trip.supplier.self.difengyun.vo.response.*;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;


/**
 * 描述：<br/>
 * 版权：Copyright (c) 2011-2020<br>
 * 公司：活力天汇<br>
 * 作者：冯志强<br>
 * 版本：1.0<br>
 * 创建日期：2020/12/8<br>
 */
@Slf4j
@Component
public class DiFengYunClientFallback implements FallbackFactory<IDiFengYunClient> {

    @Override
    public IDiFengYunClient create(Throwable throwable) {
        String msg = throwable == null ? "" : throwable.getMessage();
        if (!StringUtils.isEmpty(msg)) {
            log.error(msg);
        }
        return new IDiFengYunClient() {

            @Override
            public DfyBaseResult<DfyScenicListResponse> getScenicList(DfyBaseRequest<DfyScenicListRequest> request) {
                return null;
            }

            @Override
            public DfyBaseResult<DfyScenicDetail> getScenicDetail(DfyBaseRequest<DfyScenicDetailRequest> request) {
                return null;
            }

            @Override
            public DfyBaseResult<DfyTicketDetail> getTicketDetail(DfyBaseRequest<DfyTicketDetailRequest> request) {
                return null;
            }

            @Override
            public DfyBaseResult<DfyToursListResponse> getToursList(DfyBaseRequest<DfyToursListRequest> request) {
                return null;
            }

            @Override
            public DfyBaseResult<DfyToursDetailResponse> getToursDetail(DfyBaseRequest<DfyToursDetailRequest> request) {
                return null;
            }

            @Override
            public DfyBaseResult<DfyToursDetailResponse> getToursMultiDetail(DfyBaseRequest<DfyToursDetailRequest> request) {
                return null;
            }

            @Override
            public DfyBaseResult<List<DfyToursCalendarResponse>> getToursCalendar(@RequestBody DfyBaseRequest<DfyToursCalendarRequest> request) {
                return null;
            }

            @Override
            public DfyBaseResult<DfyOrderDetail> orderDetail(DfyBaseRequest<DfyOrderDetailRequest> request) {
                return null;
            }

            @Override
            public DfyBaseResult<DfyToursOrderDetail> toursOrderDetail(DfyBaseRequest<DfyOrderDetailRequest> request) {
                return null;
            }

            @Override
            public DfyBaseResult<DfyCreateOrderResponse> createOrder(DfyBaseRequest<DfyCreateOrderRequest> request) {
                return null;
            }

            @Override
            public DfyBaseResult cancelOrder(DfyBaseRequest<DfyCreateOrderRequest> request) {
                return null;
            }

            @Override
            public DfyBaseResult<DfyOrderStatusResponse> orderStatus(DfyBaseRequest<DfyOrderStatusRequest> request) {
                return null;
            }

            @Override
            public DfyBaseResult<DfyRefundTicketResponse> refundTicket(DfyBaseRequest<DfyRefundTicketRequest> request) {
                return null;
            }

            @Override
            public DfyBaseResult<DfySubmitOrderResponse> submitOrder(DfyBaseRequest<DfySubmitOrderRequest> request) {
                return null;
            }

            @Override
            public DfyBaseResult<DfyBillResponse> queryBill(@RequestBody DfyBaseRequest<DfyBillQueryDataReq> request){
                return null;
            }
        };

    }
}
