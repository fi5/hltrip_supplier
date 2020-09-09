package com.huoli.trip.supplier.web.hllx.service;

import com.alibaba.fastjson.JSONObject;
import com.huoli.trip.common.constant.ConfigConstants;
import com.huoli.trip.common.entity.TripOrderOperationLog;
import com.huoli.trip.common.util.ConfigGetter;
import com.huoli.trip.common.util.HttpUtil;
import com.huoli.trip.common.vo.request.central.OrderStatusKafka;
import com.huoli.trip.supplier.self.hllx.vo.HllxOrderOperationRequest;
import com.huoli.trip.supplier.self.hllx.vo.HllxRefundNoticeRequest;
import com.huoli.trip.supplier.web.mapper.TripOrderOperationLogMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class HllxSyncService {
    @Autowired
    TripOrderOperationLogMapper tripOrderOperationLogMapper;

    /**
     * 推送订单状态
     * @return
     */
    public boolean getOrderStatus(HllxOrderOperationRequest request){
        String url= ConfigGetter.getByFileItemString(ConfigConstants.CONFIG_FILE_NAME_COMMON,"hltrip.centtral")+"/recSupplier/orderStatusNotice";
        OrderStatusKafka req = new OrderStatusKafka();
        req.setOrderStatus(request.getNewStatus());
        String remark = request.getExplain();
        if(StringUtils.isNotEmpty(remark)) {
            req.setRemark(remark);
        }
        req.setPartnerOrderId(request.getOrderId());
        try {
            String res = HttpUtil.doPostWithTimeout(url, JSONObject.toJSONString(req), 10000, null);
        } catch (Exception e) {

        }
        try {
            TripOrderOperationLog tripOrderOperationLog = new TripOrderOperationLog();
            BeanUtils.copyProperties(request,tripOrderOperationLog);
            tripOrderOperationLogMapper.insertOperationLog(tripOrderOperationLog);
        }catch (Exception exception){

        }
        return true;
    }


    /**
     * 推送退款状态
     * @param request
     */
    public void refundNotice(HllxRefundNoticeRequest request){
        String url= ConfigGetter.getByFileItemString(ConfigConstants.CONFIG_FILE_NAME_COMMON,"hltrip.centtral")+"/recSupplier/refundNotice";
        try {
            String res = HttpUtil.doPostWithTimeout(url, JSONObject.toJSONString(request), 10000, null);
        } catch (Exception e) {
        }

    }


}
