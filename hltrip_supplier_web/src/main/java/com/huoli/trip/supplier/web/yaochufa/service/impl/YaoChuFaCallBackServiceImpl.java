package com.huoli.trip.supplier.web.yaochufa.service.impl;

import com.huoli.trip.supplier.self.yaochufa.vo.YcfRefundNoticeRequest;
import com.huoli.trip.supplier.self.yaochufa.vo.basevo.YcfBaseResult;
import com.huoli.trip.supplier.self.yaochufa.vo.push.OrderStatusInfo;
import com.huoli.trip.supplier.self.yaochufa.vo.push.YcfPushOrderStatusReq;
import com.huoli.trip.supplier.web.yaochufa.service.IYaoChuFaCallBackService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

/***
 * yaochufa 回调服务
 */
@Service
public class YaoChuFaCallBackServiceImpl implements IYaoChuFaCallBackService {
   public YcfBaseResult refundNotice(YcfRefundNoticeRequest request){
        //todo
        return null;
    }


    @Override
    public OrderStatusInfo synOrderStatus(YcfPushOrderStatusReq req) {
        OrderStatusInfo orderInfos = new OrderStatusInfo();
        BeanUtils.copyProperties(req,orderInfos);
//        log.info("获取到推送过来的订单状态信息: {}", orderInfos.toString());
        return orderInfos;
    }
}
