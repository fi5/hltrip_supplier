package com.huoli.trip.supplier.web.yaochufa.task;

import com.huoli.trip.common.constant.Constants;
import com.huoli.trip.common.entity.ProductPO;
import com.huoli.trip.common.entity.mpo.scenicSpotTicket.ScenicSpotProductMPO;
import com.huoli.trip.common.util.ConfigGetter;
import com.huoli.trip.common.util.DateTimeUtil;
import com.huoli.trip.supplier.api.YcfSyncService;
import com.huoli.trip.supplier.self.yaochufa.constant.YcfConfigConstants;
import com.huoli.trip.supplier.self.yaochufa.vo.YcfGetPriceRequest;
import com.huoli.trip.supplier.web.dao.ProductDao;
import com.huoli.trip.supplier.web.dao.ScenicSpotProductDao;
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
 * 创建日期：2020/8/14<br>
 */
@Component
@Slf4j
public class SyncPriceTask {

    @Value("${schedule.executor}")
    private String schedule;

    @Autowired
    private YcfSyncService ycfSyncService;

    @Autowired
    private ProductDao productDao;

    @Autowired
    private ScenicSpotProductDao scenicSpotProductDao;

    @Scheduled(cron = "0 0 2 ? * 6")
    public void syncFullPrice(){
        if(schedule == null || !StringUtils.equalsIgnoreCase("yes", schedule)){
            return;
        }
        long begin = System.currentTimeMillis();
        log.info("开始执行定时任务，同步要出发价格日历。。");
        Integer days = ConfigGetter.getByFileItemInteger(YcfConfigConstants.CONFIG_FILE_NAME, YcfConfigConstants.TASK_SYNC_PRICE_INTERVAL);
        days = days == null ? Integer.valueOf(30) : days;
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
        });
        log.info("定时任务执行完成，用时{}秒", (System.currentTimeMillis() - begin) / 1000);
    }


    // todo 真正上线的时候要发开这里，现在只为了落景点数据
//    @Scheduled(cron = "0 0 3 ? * 6")
    public void syncFullPriceV2(){
        if(schedule == null || !StringUtils.equalsIgnoreCase("yes", schedule)){
            return;
        }
        long begin = System.currentTimeMillis();
        log.info("开始执行定时任务，同步要出发价格日历。。");
        Integer days = ConfigGetter.getByFileItemInteger(YcfConfigConstants.CONFIG_FILE_NAME, YcfConfigConstants.TASK_SYNC_PRICE_INTERVAL);
        days = days == null ? Integer.valueOf(30) : days;
        String start = DateTimeUtil.formatDate(new Date());
        String end = DateTimeUtil.formatDate(DateTimeUtil.addDay(new Date(), days));
        List<ScenicSpotProductMPO> productPOs = scenicSpotProductDao.getByChannel(Constants.SUPPLIER_CODE_YCF);
        productPOs.forEach(productPO -> {
            YcfGetPriceRequest request = new YcfGetPriceRequest();
            request.setPartnerProductID(productPO.getId());
            request.setProductID(productPO.getSupplierProductId());
            request.setStartDate(start);
            request.setEndDate(end);
            ycfSyncService.syncPriceV2(request);
        });
        log.info("定时任务执行完成，用时{}秒", (System.currentTimeMillis() - begin) / 1000);
    }
}
