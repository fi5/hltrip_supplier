package com.huoli.trip.supplier.web.universal.task;

import com.huoli.trip.supplier.web.universal.service.UBRProductService;
import lombok.extern.slf4j.Slf4j;
import org.mortbay.log.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 描述：<br/>
 * 版权：Copyright (c) 2011-2020<br>
 * 公司：活力天汇<br>
 * 作者：冯志强<br>
 * 版本：1.0<br>
 * 创建日期：2021/7/29<br>
 */
@Component
@Slf4j
public class UBRTask {

    @Autowired
    private UBRProductService ubrProductService;

    @Scheduled(cron = "0 0 3 * * ?")
    public void refreshToken(){
        log.info("ubr开始刷新token定时任务。");
        ubrProductService.checkUserInfo();
        log.info("ubr刷新token完成。");
    }

    @Scheduled(cron = "0 0 0/1 * * ?")
    public void syncProduct(){
        log.info("ubr开始同步产品定时任务。");
        ubrProductService.syncProduct(null);
        log.info("ubr同步产品完成。");
    }
}
