package com.huoli.trip.supplier.web.difengyun.task;

import com.huoli.trip.common.constant.ProductType;
import com.huoli.trip.common.entity.ProductPO;
import com.huoli.trip.common.entity.mpo.groupTour.GroupTourProductMPO;
import com.huoli.trip.common.util.ListUtils;
import com.huoli.trip.supplier.self.difengyun.vo.request.DfyScenicListRequest;
import com.huoli.trip.supplier.self.difengyun.vo.request.DfyToursListRequest;
import com.huoli.trip.supplier.web.difengyun.service.DfySyncService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.huoli.trip.supplier.self.common.SupplierConstants.PRODUCT_SYNC_MODE_ONLY_ADD;
import static com.huoli.trip.supplier.self.common.SupplierConstants.PRODUCT_SYNC_MODE_ONLY_UPDATE;

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
    @Scheduled(cron = "0 0 0,6-22/3 ? * *")
    @Async
    public void syncUpdateProduct(){
        try {
            if(schedule == null || !StringUtils.equalsIgnoreCase("yes", schedule)){
                return;
            }
            long begin = System.currentTimeMillis();
            log.info("开始执行定时任务，同步笛风云产品（只更新本地已有产品）。。");
            List<ProductPO> products = dfySyncService.getSupplierProductIds(ProductType.SCENIC_TICKET.getCode());
            if(ListUtils.isEmpty(products)){
                log.error("同步笛风云产品定时任务执行完成（只更新本地已有产品），没有找到笛风云的产品。");
                return;
            }
            int i = 1;
            for (ProductPO product : products) {
                try {
                    long sTime = System.currentTimeMillis();
                    dfySyncService.syncProduct(product.getSupplierProductId(), null, PRODUCT_SYNC_MODE_ONLY_UPDATE);
                    long useTime = System.currentTimeMillis() - sTime;
                    log.info("同步第{}个产品 supplierProductCode={}，用时{}毫秒（只更新本地已有产品）", i, product.getSupplierProductId(), useTime);
                    // 如果执行时间超过310毫秒就不用睡了
                    if(useTime < 310){
                        // 限制一分钟不超过200次
                        Thread.sleep(310 - useTime);
                    }
                } catch (Exception e) {
                    log.error("同步第{}个产品supplierProductCode={}异常（只更新本地已有产品），", i, product.getSupplierProductId(), e);
                }
                i++;
            }
            log.info("同步笛风云产品定时任务执行完成（只更新本地已有产品），共{}个，用时{}秒（只更新本地已有产品）", i, (System.currentTimeMillis() - begin) / 1000);
        } catch (Exception e) {
            log.error("执行笛风云定时更新景点、产品任务异常（只更新本地已有产品）", e);
        }
    }

    /**
     * 只同步本地没有的产品，每天执行一次
     */
    @Scheduled(cron = "0 0 1 * * ?")
    @Async
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
            while (true){
                long sTime = System.currentTimeMillis();
                boolean success = dfySyncService.syncScenicList(request);
                long useTime = System.currentTimeMillis() - sTime;
                log.info("同步第{}页景点，用时{}毫秒", request.getPage(), useTime);
                if(!success) {
                    break;
                }
                request.setPage(request.getPage() + 1);
                // 如果执行时间超过310毫秒就不用睡了
                if(useTime < 310){
                    // 限制一分钟不超过200次
                    Thread.sleep(310 - useTime);
                }
            }
            log.info("同步笛风云产品定时任务执行完成，共同步{}页，用时{}秒", request.getPage(), (System.currentTimeMillis() - begin) / 1000);
        } catch (Exception e) {
            log.error("执行笛风云定时更新景点、产品任务异常", e);
        }
    }

    /**
     * 只更新本地已有产品
     */
    @Scheduled(cron = "0 0 5-23/3 ? * *")
    @Async
    public void syncUpdateToursProduct(){
        try {
            if(schedule == null || !StringUtils.equalsIgnoreCase("yes", schedule)){
                return;
            }
            long begin = System.currentTimeMillis();
            log.info("开始执行定时任务，同步笛风云跟团游产品（只更新本地已有产品）。。");
            List<ProductPO> products = dfySyncService.getSupplierProductIds(ProductType.TRIP_GROUP.getCode());
            if(ListUtils.isEmpty(products)){
                log.error("同步笛风云跟团游产品定时任务执行完成（只更新本地已有产品），没有找到笛风云的产品。");
                return;
            }
            int i = 1;
            for (ProductPO product : products) {
                try {
                    long sTime = System.currentTimeMillis();
                    dfySyncService.syncToursDetail(product.getSupplierProductId(), PRODUCT_SYNC_MODE_ONLY_UPDATE);
                    long useTime = System.currentTimeMillis() - sTime;
                    log.info("同步第{}个跟团游产品 supplierProductCode={}，用时{}毫秒（只更新本地已有产品）", i, product.getSupplierProductId(), useTime);
                    // 如果执行时间超过310毫秒就不用睡了
                    if(useTime < 310){
                        // 限制一分钟不超过200次
                        Thread.sleep(310 - useTime);
                    }
                } catch (Exception e) {
                    log.error("同步第{}个跟团游产品supplierProductCode={}异常（只更新本地已有产品），", i, product.getSupplierProductId(), e);
                }
                i++;
            }
            log.info("同步笛风云跟团游产品定时任务执行完成（只更新本地已有产品），共{}个，用时{}秒（只更新本地已有产品）", i, (System.currentTimeMillis() - begin) / 1000);
        } catch (Exception e) {
            log.error("执行笛风云定时更新跟团游产品任务异常（只更新本地已有产品）", e);
        }
    }

    /**
     * 只同步本地没有的产品，每天执行一次
     */
    @Scheduled(cron = "0 0 3 * * ?")
    @Async
    public void syncNewToursProduct(){
        try {
            if(schedule == null || !StringUtils.equalsIgnoreCase("yes", schedule)){
                return;
            }
            long begin = System.currentTimeMillis();
            log.info("开始执行定时任务，同步笛风云跟团游产品。。");
            DfyToursListRequest request = new DfyToursListRequest();
            int start = 0;
            while (true){
                request.setStart(start * 100);
                request.setLimit(100);
                long sTime = System.currentTimeMillis();
                boolean success = dfySyncService.syncToursList(request, PRODUCT_SYNC_MODE_ONLY_ADD);
                long useTime = System.currentTimeMillis() - sTime;

                log.info("同步第{}页跟团游，用时{}毫秒", (start + 1), useTime);
                if(!success) {
                    break;
                }
                // 如果执行时间超过310毫秒就不用睡了
                if(useTime < 310){
                    // 限制一分钟不超过200次
                    Thread.sleep(310 - useTime);
                }
                start++;
            }
            log.info("同步笛风云跟团游产品定时任务执行完成，共同步{}页，用时{}秒", (start + 1), (System.currentTimeMillis() - begin) / 1000);
        } catch (Exception e) {
            log.error("执行笛风云定时更新跟团游产品任务异常", e);
        }
    }

    /**
     * 只更新本地已有产品
     */
//    @Scheduled(cron = "0 0 0,6-22/3 ? * *")
    @Async
    public void syncUpdateProductV2(){
        try {
            if(schedule == null || !StringUtils.equalsIgnoreCase("yes", schedule)){
                return;
            }
            long begin = System.currentTimeMillis();
            log.info("开始执行定时任务，同步笛风云产品V2（只更新本地已有产品）。。");
            List<String> ids = dfySyncService.getSupplierProductIdsV2();
            if(ListUtils.isEmpty(ids)){
                log.error("同步笛风云产品定时任务执行完成V2（只更新本地已有产品），没有找到笛风云的产品。");
                return;
            }
            int i = 1;
            for (String id : ids) {
                try {
                    long sTime = System.currentTimeMillis();
                    dfySyncService.syncProduct(id, null, PRODUCT_SYNC_MODE_ONLY_UPDATE);
                    long useTime = System.currentTimeMillis() - sTime;
                    log.info("同步第{}个产品V2 supplierProductCode={}，用时{}毫秒（只更新本地已有产品）", i, id, useTime);
                    // 如果执行时间超过310毫秒就不用睡了
                    if(useTime < 310){
                        // 限制一分钟不超过200次
                        Thread.sleep(310 - useTime);
                    }
                } catch (Exception e) {
                    log.error("同步第{}个产品supplierProductCode={}异常V2（只更新本地已有产品），", i, id, e);
                }
                i++;
            }
            log.info("同步笛风云产品定时任务执行完成（只更新本地已有产品）V2，共{}个，用时{}秒（只更新本地已有产品）", i, (System.currentTimeMillis() - begin) / 1000);
        } catch (Exception e) {
            log.error("执行笛风云定时更新景点、产品任务异常V2（只更新本地已有产品）", e);
        }
    }

    /**
     * 只同步本地没有的产品，每天执行一次
     */
//    @Scheduled(cron = "0 0 1 * * ?")
    @Async
    public void syncNewProductV2(){
        try {
            if(schedule == null || !StringUtils.equalsIgnoreCase("yes", schedule)){
                return;
            }
            long begin = System.currentTimeMillis();
            log.info("开始执行定时任务V2，同步笛风云产品。。");
            DfyScenicListRequest request = new DfyScenicListRequest();
            request.setPage(1);
            request.setPageSize(100);
            while (true){
                long sTime = System.currentTimeMillis();
                boolean success = dfySyncService.syncScenicListV2(request);
                long useTime = System.currentTimeMillis() - sTime;
                log.info("同步第{}页景点V2，用时{}毫秒", request.getPage(), useTime);
                if(!success) {
                    break;
                }
                request.setPage(request.getPage() + 1);
                // 如果执行时间超过310毫秒就不用睡了
                if(useTime < 310){
                    // 限制一分钟不超过200次
                    Thread.sleep(310 - useTime);
                }
            }
            log.info("同步笛风云产品定时任务执行完成V2，共同步{}页，用时{}秒", request.getPage(), (System.currentTimeMillis() - begin) / 1000);
        } catch (Exception e) {
            log.error("执行笛风云定时更新景点、产品任务异常V2", e);
        }
    }

    /**
     * 只更新本地已有产品
     */
//    @Scheduled(cron = "0 0 5-23/3 ? * *")
    @Async
    public void syncUpdateToursProductV2(){
        try {
            if(schedule == null || !StringUtils.equalsIgnoreCase("yes", schedule)){
                return;
            }
            long begin = System.currentTimeMillis();
            log.info("开始执行定时任务，同步笛风云跟团游产品V2（只更新本地已有产品）。。");
            List<String> ids = dfySyncService.getSupplierToursProductIdsV2();
            if(ListUtils.isEmpty(ids)){
                log.error("同步笛风云跟团游产品定时任务执行完成V2（只更新本地已有产品），没有找到笛风云的产品。");
                return;
            }
            int i = 1;
            for (String id : ids) {
                try {
                    long sTime = System.currentTimeMillis();
                    dfySyncService.syncToursDetailV2(id);
                    long useTime = System.currentTimeMillis() - sTime;
                    log.info("同步第{}个跟团游产品V2 supplierProductCode={}，用时{}毫秒（只更新本地已有产品）", i, id, useTime);
                    // 如果执行时间超过310毫秒就不用睡了
                    if(useTime < 310){
                        // 限制一分钟不超过200次
                        Thread.sleep(310 - useTime);
                    }
                } catch (Exception e) {
                    log.error("同步第{}个跟团游产品supplierProductCode={}异常V2（只更新本地已有产品），", i, id, e);
                }
                i++;
            }
            log.info("同步笛风云跟团游产品定时任务执行完成V2（只更新本地已有产品），共{}个，用时{}秒（只更新本地已有产品）", i, (System.currentTimeMillis() - begin) / 1000);
        } catch (Exception e) {
            log.error("执行笛风云定时更新跟团游产品任务异常V2（只更新本地已有产品）", e);
        }
    }

    /**
     * 只同步本地没有的产品，每天执行一次
     */
//    @Scheduled(cron = "0 0 3 * * ?")
    @Async
    public void syncNewToursProductV2(){
        try {
            if(schedule == null || !StringUtils.equalsIgnoreCase("yes", schedule)){
                return;
            }
            long begin = System.currentTimeMillis();
            log.info("开始执行定时任务，同步笛风云跟团游产品V2。。");
            DfyToursListRequest request = new DfyToursListRequest();
            int start = 0;
            while (true){
                request.setStart(start * 100);
                request.setLimit(100);
                long sTime = System.currentTimeMillis();
                boolean success = dfySyncService.syncToursListV2(request);
                long useTime = System.currentTimeMillis() - sTime;

                log.info("同步第{}页跟团游V2，用时{}毫秒", (start + 1), useTime);
                if(!success) {
                    break;
                }
                // 如果执行时间超过310毫秒就不用睡了
                if(useTime < 310){
                    // 限制一分钟不超过200次
                    Thread.sleep(310 - useTime);
                }
                start++;
            }
            log.info("同步笛风云跟团游产品定时任务执行完成V2，共同步{}页，用时{}秒", (start + 1), (System.currentTimeMillis() - begin) / 1000);
        } catch (Exception e) {
            log.error("执行笛风云定时更新跟团游产品任务异常V2", e);
        }
    }
}
