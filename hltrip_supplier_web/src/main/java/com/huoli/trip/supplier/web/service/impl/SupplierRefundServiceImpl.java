package com.huoli.trip.supplier.web.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSONObject;
import com.huoli.trip.common.constant.CentralError;
import com.huoli.trip.common.constant.ConfigConstants;
import com.huoli.trip.common.util.ConfigGetter;
import com.huoli.trip.common.util.HttpUtil;
import com.huoli.trip.common.vo.request.RefundNoticeReq;
import com.huoli.trip.common.vo.response.BaseResponse;
import com.huoli.trip.supplier.web.service.SupplierRefundService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @author :zhouwenbin
 * @time   :2020/9/7
 * @comment:
 **/
@Service
@Slf4j
public class SupplierRefundServiceImpl implements SupplierRefundService {
	@Override
	public BaseResponse doRefund(@RequestBody RefundNoticeReq req) {
		String url= ConfigGetter.getByFileItemString(ConfigConstants.CONFIG_FILE_NAME_COMMON,"hltrip.centtral")+"/recSupplier/refundNotice";
		try {

			log.info("请求的地址:"+url+",参数:"+ JSONObject.toJSONString(req));
			String res = HttpUtil.doPostWithTimeout(url, JSONObject.toJSONString(req), 10000, null);
			log.info("中台refundNotice返回:"+res);

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

			log.info("请求的地址:"+url+",参数:"+ JSONObject.toJSONString(req));
			String res = HttpUtil.doPostWithTimeout(url, JSONObject.toJSONString(req), 10000, null);
			log.info("中台refundNotice返回:"+res);
		} catch (Exception e) {
			log.error("信息{}",e);
			return BaseResponse.fail(CentralError.ERROR_UNKNOWN);
		}
		return BaseResponse.withSuccess();
	}
}
