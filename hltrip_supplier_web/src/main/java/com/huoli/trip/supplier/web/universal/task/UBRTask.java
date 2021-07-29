package com.huoli.trip.supplier.web.universal.task;

import com.huoli.trip.supplier.web.universal.service.UBRProductService;
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
public class UBRTask {

    @Autowired
    private UBRProductService ubrProductService;

    @Scheduled(cron = "0 0 3 * * ?")
    public void refreshToken(){
        ubrProductService.checkUserInfo();
    }
}
