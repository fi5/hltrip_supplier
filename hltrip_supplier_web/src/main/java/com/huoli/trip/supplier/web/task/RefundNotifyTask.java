package com.huoli.trip.supplier.web.task;

import com.huoli.trip.common.entity.ProductItemPO;
import com.huoli.trip.common.entity.TripRefundNotify;
import com.huoli.trip.common.util.DateTimeUtil;
import com.huoli.trip.supplier.api.DfyOrderService;
import com.huoli.trip.supplier.api.DynamicProductItemService;
import com.huoli.trip.supplier.web.dao.ProductItemDao;
import com.huoli.trip.supplier.web.mapper.TripOrderRefundMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
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

    @Scheduled(cron="0 0 0/1 * * ?")
    public void notifyRefund(){
        if(schedule == null || !StringUtils.equalsIgnoreCase("yes", schedule)){
            return;
        }
        long s = System.currentTimeMillis();
        log.info("开始执行notifyRefund任务。。。");
        List<TripRefundNotify> pendingNotifys = tripOrderRefundMapper.getPendingNotifys();
        pendingNotifys.forEach(item -> {
            try {
                dfyOrderService.processNotify(item);
                Thread.sleep(100);
            } catch (InterruptedException e) {
                log.error("处理退款通知失败", e);
            } catch (Exception e) {
                log.error("处理退款通知失败了，id={}", item.getId(), e);
            }
        });
        long t = System.currentTimeMillis() - s;
        log.info("执行notifyRefund任务。用时{}", DateTimeUtil.format(DateTimeUtil.toGreenWichTime(new Date(t)), "HH:mm:ss"));
    }
}
