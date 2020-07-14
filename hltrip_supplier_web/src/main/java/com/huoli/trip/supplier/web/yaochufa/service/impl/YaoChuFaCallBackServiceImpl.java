package com.huoli.trip.supplier.web.yaochufa.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.huoli.trip.common.constant.ConfigConstants;
import com.huoli.trip.common.util.ConfigGetter;
import com.huoli.trip.common.util.HttpUtil;
import com.huoli.trip.supplier.self.yaochufa.vo.YcfRefundNoticeRequest;
import com.huoli.trip.supplier.self.yaochufa.vo.push.YcfPushOrderStatusReq;
import com.huoli.trip.supplier.web.yaochufa.service.IYaoChuFaCallBackService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/***
 * yaochufa 回调服务
 */
@Service
@Slf4j
public class YaoChuFaCallBackServiceImpl implements IYaoChuFaCallBackService {

   public void refundNotice(YcfRefundNoticeRequest request){
       String url= ConfigGetter.getByFileItemString(ConfigConstants.CONFIG_FILE_NAME_COMMON,"hltrip.centtral")+"/recSupplier/refundNotice";
       try {

           String res = HttpUtil.doPostWithTimeout(url, JSONObject.toJSONString(request), 10000, null);
           log.info("中台refundNotice返回:"+res);
       } catch (Exception e) {
           log.info("",e);
       }

   }


    @Override
    public void orderStatusNotice(YcfPushOrderStatusReq req) {
        String url= ConfigGetter.getByFileItemString(ConfigConstants.CONFIG_FILE_NAME_COMMON,"hltrip.centtral")+"/recSupplier/orderStatusNotice";
//        String url= "http://192.168.11.138:9061/hltrip_central/recSupplier/orderStatusNotice";
        try {
            String string = JSONObject.toJSONString(req);
            log.info("中台订单推送传参json:"+string);
            String res = HttpUtil.doPostWithTimeout(url, JSONObject.toJSONString(req), 10000, null);
            log.info("中台orderStatusNotice返回:"+res);
        } catch (Exception e) {
            log.info("",e);
        }
//        OrderStatusInfo orderInfos = new OrderStatusInfo();
//        BeanUtils.copyProperties(req,orderInfos);
////        log.info("获取到推送过来的订单状态信息: {}", orderInfos.toString());
//        return orderInfos;
    }

}
