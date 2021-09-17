package com.huoli.trip.supplier.web.task;

import com.huoli.eagle.eye.core.HuoliTrace;
import com.huoli.trip.common.entity.ProductItemPO;
import com.huoli.trip.common.entity.TripRefundNotify;
import com.huoli.trip.common.util.DateTimeUtil;
import com.huoli.trip.supplier.api.DfyOrderService;
import com.huoli.trip.supplier.api.DynamicProductItemService;
import com.huoli.trip.supplier.api.UBROrderService;
import com.huoli.trip.supplier.web.dao.ProductItemDao;
import com.huoli.trip.supplier.web.mapper.TripOrderRefundMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

/**
 * 描述：<br/>
 @author :zhouwenbin
 */
@Component
@Slf4j
public class RefundNotifyTask {

    @Value("${schedule.executor}")
    private String schedule;

    @Autowired
    TripOrderRefundMapper tripOrderRefundMapper;
    @Autowired
    DfyOrderService dfyOrderService;

    @Autowired
    private UBROrderService ubrOrderService;

    @Autowired
    private ThreadPoolTaskExecutor threadPool;

    @Autowired
    private HuoliTrace huoliTrace;

    @Scheduled(cron="0 0 0/1 * * ?")
    public void notifyRefundDFY(){
        if(schedule == null || !StringUtils.equalsIgnoreCase("yes", schedule)){
            return;
        }
        log.info("开始执行notifyRefundDFY任务。。。");
        threadPool.execute(() -> {
            huoliTrace.continueSpan(huoliTrace.currentSpan());
            long s = System.currentTimeMillis();
            try {
                dfyOrderService.processNotifyTicket();
                long t = System.currentTimeMillis() - s;
                log.info("执行笛风云门票通知退款任务完成。用时{}", DateTimeUtil.format(DateTimeUtil.toGreenWichTime(new Date(t)), "HH:mm:ss"));
            } catch (Exception e) {
                log.error("笛风云门票通知退款异常", e);
            }
            s = System.currentTimeMillis();
            try {
                dfyOrderService.processNotifyTour();
                long t = System.currentTimeMillis() - s;
                log.info("执行笛风云跟团游通知退款任务完成。用时{}", DateTimeUtil.format(DateTimeUtil.toGreenWichTime(new Date(t)), "HH:mm:ss"));
            } catch (Exception e) {
                log.error("笛风云跟团游通知退款异常", e);
            }
        });
        log.info("执行notifyRefundDFY任务完成");
    }

    @Scheduled(cron="0 0/30 * * * ?")
    public void notifyRefundBTG(){
        if(schedule == null || !StringUtils.equalsIgnoreCase("yes", schedule)){
            return;
        }
        log.info("开始执行notifyRefundBTG任务。。。");
        threadPool.execute(() -> {
            try {
                huoliTrace.continueSpan(huoliTrace.currentSpan());
                log.info("异步执行notifyRefundBTG任务。。。");
                long s = System.currentTimeMillis();
                ubrOrderService.processNotify();
                long t = System.currentTimeMillis() - s;
                log.info("执行btg通知退款任务。用时{}", DateTimeUtil.format(DateTimeUtil.toGreenWichTime(new Date(t)), "HH:mm:ss"));
            } catch (Exception e) {
                log.error("btg通知退款异常", e);
            }
        });
        log.info("执行notifyRefundBTG任务完成");
    }
}
