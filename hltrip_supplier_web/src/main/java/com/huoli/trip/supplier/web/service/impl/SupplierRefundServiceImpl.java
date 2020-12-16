package com.huoli.trip.supplier.web.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSONObject;
import com.huoli.trip.common.constant.CentralError;
import com.huoli.trip.common.constant.ConfigConstants;
import com.huoli.trip.common.constant.OrderStatus;
import com.huoli.trip.common.entity.TripOrder;
import com.huoli.trip.common.entity.TripOrderOperationLog;
import com.huoli.trip.common.entity.TripOrderRefund;
import com.huoli.trip.common.util.ConfigGetter;
import com.huoli.trip.common.util.DateTimeUtil;
import com.huoli.trip.common.util.HttpUtil;
import com.huoli.trip.common.vo.request.RefundNoticeReq;
import com.huoli.trip.common.vo.response.BaseResponse;
import com.huoli.trip.supplier.self.hllx.vo.HllxOrderOperationRequest;
import com.huoli.trip.supplier.web.hllx.service.HllxSyncService;
import com.huoli.trip.supplier.web.mapper.TripOrderMapper;
import com.huoli.trip.supplier.web.mapper.TripOrderOperationLogMapper;
import com.huoli.trip.supplier.web.mapper.TripOrderRefundMapper;
import com.huoli.trip.supplier.web.service.SupplierRefundService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author :zhouwenbin
 * @time   :2020/9/7
 * @comment:
 **/
@Service
@Slf4j
public class SupplierRefundServiceImpl implements SupplierRefundService {

	@Autowired
	TripOrderOperationLogMapper tripOrderOperationLogMapper;

	@Autowired
	HllxSyncService hllxSyncService;
	@Autowired
	TripOrderMapper tripOrderMapper;
	@Autowired
	TripOrderRefundMapper tripOrderRefundMapper;

	@Override
	public BaseResponse doRefund(@RequestBody RefundNoticeReq req) {
		String url= ConfigGetter.getByFileItemString(ConfigConstants.CONFIG_FILE_NAME_COMMON,"hltrip.centtral")+"/recSupplier/refundNotice";
		try {

			TripOrderRefund refundOrder = tripOrderRefundMapper.getRefundOrderByOrderId(req.getPartnerOrderId());
			if(refundOrder==null)
				return BaseResponse.fail(CentralError.ERROR_ORDER_TRIP_ORDER_RRFUND_ERROR);
			if(refundOrder.getStatus()!=0)
				return BaseResponse.fail(CentralError.ERROR_ORDER_TRIP_ORDER_RRFUND_STATUS_ERROR);


			log.info("doRefund请求的地址:"+url+",参数:"+ JSONObject.toJSONString(req)+"refundStatus:"+refundOrder.getStatus());
			String res = HttpUtil.doPostWithTimeout(url, JSONObject.toJSONString(req), 10000, null);
			log.info("中台refundNotice返回:"+res);
			int newSt=OrderStatus.REFUNDED.getCode();
			String explain="退款";
//			if(req.getRefundStatus()!=1){
//				newSt=OrderStatus.WAITING_TO_TRAVEL.getCode();
//				explain="拒绝退订";
//			}
			TripOrder tripOrder = tripOrderMapper.getOrderStatusByOrderId(req.getPartnerOrderId());
			int oldSt=tripOrder.getChannelStatus();

			HllxOrderOperationRequest request=new HllxOrderOperationRequest();
			request.setOrderId(req.getPartnerOrderId());
			request.setOperator(req.getOperator());
			request.setOldStatus(oldSt);
			request.setNewStatus(newSt);
			request.setUpdateTime(DateTimeUtil.formatFullDate(new Date()));
			request.setExplain(explain);
			hllxSyncService.getOrderStatus(request);

//			TripOrderOperationLog tripOrderOperationLog = new TripOrderOperationLog();
//			tripOrderOperationLog.setOrderId(req.getPartnerOrderId());
//			tripOrderOperationLog.setOperator(req.getOperator());
//			tripOrderOperationLog.setNewStatus(newSt);
//			tripOrderOperationLog.setUpdateTime(DateTimeUtil.formatFullDate(new Date()));
//			tripOrderOperationLog.setRemark("操作退款"+req.getHandleRemark());
//			tripOrderOperationLogMapper.insertOperationLog(tripOrderOperationLog);

		} catch (Exception e) {
			log.error("信息{}",e);
			return BaseResponse.fail(CentralError.ERROR_UNKNOWN);
		}
		return BaseResponse.withSuccess();
	}

	@Override
	public BaseResponse refuseRefund(@RequestBody RefundNoticeReq req) {
		String url= ConfigGetter.getByFileItemString(ConfigConstants.CONFIG_FILE_NAME_COMMON,"hltrip.centtral")+"/recSupplier/refundNotice";
		try {

			req.setRefundStatus(2);//表示拒绝退订
			log.info("refuseRefund请求的地址:"+url+",参数:"+ JSONObject.toJSONString(req));
			String res = HttpUtil.doPostWithTimeout(url, JSONObject.toJSONString(req), 10000, null);
			log.info("中台refundNotice返回:"+res);
		} catch (Exception e) {
			log.error("信息{}",e);
			return BaseResponse.fail(CentralError.ERROR_UNKNOWN);
		}
		return BaseResponse.withSuccess();
	}
}
