package com.huoli.trip.supplier.web.util;

import com.alibaba.dubbo.config.annotation.Reference;
import com.huoli.flight.server.api.SmsService;
import com.huoli.flight.server.api.vo.SmsReq;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author lunatic
 * @Title: SendMessageService
 * @Package
 * @Description: 封装短信发送服务
 * @date 2020/12/214:48
 */
@Slf4j
@Component
public class SendMessageUtil {

/*    @Reference(group = "${dubbo_group}", timeout = 30000, *//*check = false,*//* retries = 3)*/
    @Reference(group = "flight-dubbo-online")
    private SmsService smsService;

    private static final String content1 = "您好，您有新的旅游产品支付订单，订单号：%S；请尽快前往后台管理系统处理，谢谢";
    private static final String content2 = "您好，您有旅游产品退款申请订单，订单号：%S；请尽快前往后台管理系统处理，谢谢。";


    public void sendMSG(String phone,String orderId,int type){
        SmsReq smsReq = new SmsReq();
        SmsReq.Sms sms = new SmsReq.Sms();
        if(type ==1) {
            sms.setContent(String.format(content1, orderId));
        }else{
            sms.setContent(String.format(content2, orderId));
        }
        smsReq.setSms(sms);
        smsReq.setPhone(phone);
        try {
            smsService.sendSms(smsReq);
        }catch (Exception exception){
            log.error("发送短信出现异常",exception);
        }
    }

}
