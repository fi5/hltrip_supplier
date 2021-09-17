package com.huoli.trip.supplier.web.controller;

import com.huoli.trip.common.vo.request.RefundNoticeReq;
import com.huoli.trip.common.vo.response.BaseResponse;
import com.huoli.trip.supplier.api.UBROrderService;
import com.huoli.trip.supplier.web.service.SupplierRefundService;
import com.huoli.trip.supplier.web.task.RefundNotifyTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author :zhouwenbin
 * @time   :2020/9/7
 * @comment:
 **/
@RestController
@RequestMapping(value = "/order/refund", produces = "application/json")
public class RefundController {

	@Autowired
	SupplierRefundService supplierRefundService;

	@Autowired
	private RefundNotifyTask refundNotifyTask;

	@Autowired
	private UBROrderService ubrOrderService;

	@RequestMapping("/doRefund")
	public BaseResponse doRefund(@RequestBody RefundNoticeReq req)  {
		return supplierRefundService.doRefund(req);
	}
	@RequestMapping("/tt")
	public BaseResponse tt()  {
		return BaseResponse.withSuccess();
	}
//
	@RequestMapping("/refuse")
	public BaseResponse refuseRefund(@RequestBody RefundNoticeReq req)  {
		return supplierRefundService.refuseRefund(req);
	}

	@PostMapping("/notify/difengyun")
	public BaseResponse refundNotifyDFY()  {
		refundNotifyTask.notifyRefundDFY();
		return BaseResponse.withSuccess();
	}

	@PostMapping("/notify/btg")
	public BaseResponse refundNotifyBTG()  {
		ubrOrderService.processNotify();
		return BaseResponse.withSuccess();
	}
}
