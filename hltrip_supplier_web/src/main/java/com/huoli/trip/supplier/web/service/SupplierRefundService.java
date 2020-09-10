package com.huoli.trip.supplier.web.service;

import com.huoli.trip.common.vo.request.RefundNoticeReq;
import com.huoli.trip.common.vo.response.BaseResponse;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author :zhouwenbin
 * @time   :2020/9/7
 * @comment:
 **/
public interface SupplierRefundService {

	 BaseResponse doRefund(@RequestBody RefundNoticeReq req) ;

	 BaseResponse refuseRefund(@RequestBody RefundNoticeReq req) ;
}
