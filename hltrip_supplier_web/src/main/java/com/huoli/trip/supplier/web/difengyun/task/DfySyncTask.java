package com.huoli.trip.supplier.web.difengyun.task;

import com.huoli.trip.common.util.ListUtils;
import com.huoli.trip.supplier.self.difengyun.constant.DfyConstants;
import com.huoli.trip.supplier.self.difengyun.vo.request.DfyScenicListRequest;
import com.huoli.trip.supplier.web.difengyun.service.DfySyncService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 描述：<br/>
 * 版权：Copyright (c) 2011-2020<br>
 * 公司：活力天汇<br>
 * 作者：冯志强<br>
 * 版本：1.0<br>
 * 创建日期：2020/12/10<br>
 */
@Slf4j
@Component
public class DfySyncTask {

    @Value("${schedule.executor}")
    private String schedule;

    @Autowired
    private DfySyncService dfySyncService;

    /**
     * 只更新本地已有产品
     */
    @Scheduled(cron = "0 0 0,6-22/2 ? * *")
    public void syncUpdateProduct(){
        try {
            if(schedule == null || !StringUtils.equalsIgnoreCase("yes", schedule)){
                return;
            }
            long begin = System.currentTimeMillis();
            log.info("开始执行定时任务，同步笛风云产品（只更新本地已有产品）。。");
            List<String> ids = dfySyncService.getSupplierProductIds();
            if(ListUtils.isEmpty(ids)){
                log.error("同步笛风云产品定时任务执行完成（只更新本地已有产品），没有找到笛风云的产品。");
                return;
            }
            ids.forEach(id -> {
                try {
                    dfySyncService.syncProduct(id, null, DfyConstants.PRODUCT_SYNC_MODE_ONLY_UPDATE);
                    // 限制一分钟不超过200次
                    Thread.sleep(310);
                } catch (Exception e) {
                    log.error("同步产品supplierProductCode={}异常，", e);
                }
            });
            log.info("同步笛风云产品定时任务执行完成（只更新本地已有产品），用时{}秒", (System.currentTimeMillis() - begin) / 1000);
        } catch (Exception e) {
            log.error("执行笛风云定时更新景点、产品任务异常（只更新本地已有产品）", e);
        }
    }

    /**
     * 只同步本地没有的产品，每天执行一次
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void syncNewProduct(){
        try {
            if(schedule == null || !StringUtils.equalsIgnoreCase("yes", schedule)){
                return;
            }
            long begin = System.currentTimeMillis();
            log.info("开始执行定时任务，同步笛风云产品。。");
            DfyScenicListRequest request = new DfyScenicListRequest();
            request.setPage(1);
            request.setPageSize(100);
            while (dfySyncService.syncScenicList(request)){
                request.setPage(request.getPage() + 1);
                // 限制一分钟不超过200次
                Thread.sleep(310);
            }
            log.info("同步笛风云产品定时任务执行完成，用时{}秒", (System.currentTimeMillis() - begin) / 1000);
        } catch (Exception e) {
            log.error("执行笛风云定时更新景点、产品任务异常", e);
        }
    }
}
