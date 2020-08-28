package com.huoli.trip.supplier.web.task;

import com.huoli.trip.common.entity.ProductItemPO;
import com.huoli.trip.common.util.DateTimeUtil;
import com.huoli.trip.supplier.api.DynamicProductItemService;
import com.huoli.trip.supplier.web.dao.ProductItemDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

/**
 * 描述：<br/>
 * 版权：Copyright (c) 2011-2020<br>
 * 公司：活力天汇<br>
 * 作者：冯志强<br>
 * 版本：1.0<br>
 * 创建日期：2020/8/28<br>
 */
@Component
@Slf4j
public class RefreshItemTask {

    @Autowired
    private ProductItemDao productItemDao;

    @Autowired
    private DynamicProductItemService dynamicProductItemService;

    @Scheduled(cron = "0 10 0 * * ?")
    public void refreshItemProduct(){
        long s = System.currentTimeMillis();
        log.info("开始执行刷新item低价产品任务。。。");
        List<ProductItemPO> items = productItemDao.selectCodes();
        items.forEach(item -> {
            dynamicProductItemService.refreshItemByCode(item.getCode());
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                log.error("刷新item定时任务线程休眠失败", e);
            }
        });
        long t = System.currentTimeMillis() - s;
        log.info("刷新item低价产品任务执行完毕。用时{}", DateTimeUtil.format(DateTimeUtil.toGreenWichTime(new Date(t)), "HH:mm:ss"));
    }
}
