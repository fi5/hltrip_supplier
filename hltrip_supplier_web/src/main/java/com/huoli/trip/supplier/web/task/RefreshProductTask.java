package com.huoli.trip.supplier.web.task;

import com.huoli.trip.common.constant.Constants;
import com.huoli.trip.common.entity.ProductPO;
import com.huoli.trip.common.util.DateTimeUtil;
import com.huoli.trip.supplier.web.dao.ProductDao;
import com.huoli.trip.supplier.web.service.CommonService;
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
 * 版权：Copyright (c) 2011-2020<br>
 * 公司：活力天汇<br>
 * 作者：冯志强<br>
 * 版本：1.0<br>
 * 创建日期：2021/1/11<br>
 */
@Slf4j
@Component
public class RefreshProductTask {

    @Value("${schedule.executor}")
    private String schedule;

    @Autowired
    private ProductDao productDao;

    @Autowired
    private CommonService commonService;

    @Scheduled(cron = "0 0 3 * * ?")
    public void refreshItemProduct(){
        if(schedule == null || !StringUtils.equalsIgnoreCase("yes", schedule)){
            return;
        }
        long s = System.currentTimeMillis();
        log.info("开始执行刷新产品状态任务。。。");
        List<ProductPO> products = productDao.getProductsByStatus(Constants.PRODUCT_STATUS_VALID);
        Date date = DateTimeUtil.trancateToDate(new Date());
        products.forEach(productPO -> {
            commonService.checkProduct(productPO, date);
        });
        long t = System.currentTimeMillis() - s;
        log.info("刷新产品状态任务执行完毕。用时{}", DateTimeUtil.format(DateTimeUtil.toGreenWichTime(new Date(t)), "HH:mm:ss"));
    }

}
