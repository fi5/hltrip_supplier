package com.huoli.trip.supplier.web.yaochufa.task;

import com.huoli.trip.common.constant.Constants;
import com.huoli.trip.common.entity.ProductPO;
import com.huoli.trip.common.util.ConfigGetter;
import com.huoli.trip.common.util.DateTimeUtil;
import com.huoli.trip.supplier.api.YcfSyncService;
import com.huoli.trip.supplier.self.yaochufa.constant.YcfConfigConstants;
import com.huoli.trip.supplier.self.yaochufa.vo.YcfGetPriceRequest;
import com.huoli.trip.supplier.web.dao.ProductDao;
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
 * 创建日期：2020/8/14<br>
 */
@Component
@Slf4j
public class SyncPriceTask {

    @Autowired
    private YcfSyncService ycfSyncService;

    @Autowired
    private ProductDao productDao;

    @Scheduled(cron = "0 0 2 ? * 6")
    public void syncFullPrice(){
        long begin = System.currentTimeMillis();
        log.info("开始执行定时任务，同步要出发价格日历。。");
        Integer days = ConfigGetter.getByFileItemInteger(YcfConfigConstants.CONFIG_FILE_NAME, YcfConfigConstants.TASK_SYNC_PRICE_INTERVAL);
        days = days == null ? 30 : days;
        String start = DateTimeUtil.formatDate(new Date());
        String end = DateTimeUtil.formatDate(DateTimeUtil.addDay(new Date(), days));
        List<ProductPO> productPOs =  productDao.getCodeBySupplierId(Constants.SUPPLIER_CODE_YCF);
        productPOs.forEach(productPO -> {
            YcfGetPriceRequest request = new YcfGetPriceRequest();
//            request.setFull(true);
            request.setPartnerProductID(productPO.getCode());
            request.setProductID(productPO.getSupplierProductId());
            request.setStartDate(start);
            request.setEndDate(end);
            ycfSyncService.getPrice(request);
            try {
                Thread.sleep(310);
            } catch (InterruptedException e) {
                log.error("线程暂停失败", e);
            }
        });
        log.info("定时任务执行完成，用时{}秒", (System.currentTimeMillis() - begin) / 1000);
    }
}
