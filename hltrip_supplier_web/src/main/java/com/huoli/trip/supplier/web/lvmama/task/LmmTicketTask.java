package com.huoli.trip.supplier.web.lvmama.task;

import com.huoli.trip.common.util.DateTimeUtil;
import com.huoli.trip.common.util.ListUtils;
import com.huoli.trip.supplier.self.lvmama.vo.request.LmmProductListRequest;
import com.huoli.trip.supplier.self.lvmama.vo.request.LmmScenicListRequest;
import com.huoli.trip.supplier.web.lvmama.service.LmmSyncService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.huoli.trip.supplier.self.common.SupplierConstants.PRODUCT_SYNC_MODE_ONLY_UPDATE;
import static com.huoli.trip.supplier.self.common.SupplierConstants.PRODUCT_SYNC_MODE_UNLIMITED;
import static com.huoli.trip.supplier.self.difengyun.constant.DfyConstants.*;

/**
 * 描述：<br/>
 * 版权：Copyright (c) 2011-2020<br>
 * 公司：活力天汇<br>
 * 作者：冯志强<br>
 * 版本：1.0<br>
 * 创建日期：2021/3/18<br>
 */
@Slf4j
@Component
public class LmmTicketTask {

    @Value("${schedule.executor}")
    private String schedule;

    @Autowired
    private LmmSyncService lmmScenicService;

    /**
     * 只更新本地已有景点
     */
//    @Scheduled(cron = "0 0 0,6-22/3 ? * *")
    public void syncUpdateScenic(){
        try {
            if(schedule == null || !StringUtils.equalsIgnoreCase("yes", schedule)){
                return;
            }
            long begin = System.currentTimeMillis();
            log.info("开始执行定时任务，同步驴妈妈景点（只更新本地已有景点）。。");
            List<String> ids = lmmScenicService.getSupplierScenicIds();
            if(ListUtils.isEmpty(ids)){
                log.error("同步驴妈妈景点定时任务执行完成（只更新本地已有景点），没有找到驴妈妈景点。");
                return;
            }
            int i = 1;
            for (String id : ids) {
                try {
                    long sTime = System.currentTimeMillis();
                    lmmScenicService.syncScenicListById(id);
                    long useTime = System.currentTimeMillis() - sTime;
                    log.info("同步第{}个景点 scenicId={}，用时{}毫秒（只更新本地已有景点）",
                            i, id, useTime);
                    // 如果执行时间超过310毫秒就不用睡了
                    if(useTime < 310){
                        // 限制一分钟不超过200次
                        Thread.sleep(310 - useTime);
                    }
                } catch (Exception e) {
                    log.error("同步第{}个景点scenicId={}异常（只更新本地已有景点），", i, id, e);
                }
                i++;
            }
            log.info("同步驴妈妈景点定时任务执行完成（只更新本地已有景点），共{}个，用时{}秒（只更新本地已有景点）", i, (System.currentTimeMillis() - begin) / 1000);
        } catch (Exception e) {
            log.error("执行驴妈妈定时更新景点任务异常（只更新本地已有景点）", e);
        }
    }

    /**
     * 只同步本地没有的景点，每周一次
     */
//    @Scheduled(cron = "0 0 1 ? * SUN")
    public void syncNewScenic(){
        try {
            if(schedule == null || !StringUtils.equalsIgnoreCase("yes", schedule)){
                return;
            }
            long begin = System.currentTimeMillis();
            log.info("开始执行定时任务，同步驴妈妈景点。。(拉取本地没有的景点)");
            LmmScenicListRequest request = new LmmScenicListRequest();
            request.setCurrentPage(1);
            while (true){
                long sTime = System.currentTimeMillis();
                boolean success = lmmScenicService.syncScenicList(request);
                long useTime = System.currentTimeMillis() - sTime;
                log.info("同步第{}页景点，用时{}毫秒，(拉取本地没有的景点)", request.getCurrentPage(), useTime);
                if(!success) {
                    break;
                }
                request.setCurrentPage(request.getCurrentPage() + 1);
                // 如果执行时间超过310毫秒就不用睡了
                if(useTime < 310){
                    // 限制一分钟不超过200次
                    Thread.sleep(310 - useTime);
                }
            }
            log.info("同步驴妈妈景点定时任务执行完成，共同步{}页，用时{}秒，(拉取本地没有的景点)", request.getCurrentPage(), (System.currentTimeMillis() - begin) / 1000);
        } catch (Exception e) {
            log.error("执行驴妈妈定时更新景点任务异常，(拉取本地没有的景点)", e);
        }
    }

    /**
     * 只更新本地已有商品
     * 驴妈妈不需要，一周全量同步一次
     */
//    @Scheduled(cron = "0 0 1,6-22/3 ? * *")
    public void syncUpdateProduct(){
        try {
            if(schedule == null || !StringUtils.equalsIgnoreCase("yes", schedule)){
                return;
            }
            long begin = System.currentTimeMillis();
            log.info("开始执行定时任务，同步驴妈妈商品（只更新本地已有商品）。。");
            List<String> ids = lmmScenicService.getSupplierProductIds();
            if(ListUtils.isEmpty(ids)){
                log.error("同步驴妈妈商品定时任务执行完成（只更新本地已有商品），没有找到驴妈妈商品。");
                return;
            }
            int i = 1;
            for (String id : ids) {
                try {
                    long sTime = System.currentTimeMillis();
                    lmmScenicService.syncGoodsListById(id, PRODUCT_SYNC_MODE_ONLY_UPDATE);
                    long useTime = System.currentTimeMillis() - sTime;
                    log.info("同步第{}个商品 scenicId={}，用时{}毫秒（只更新本地已有商品）",
                            i, id, useTime);
                    // 如果执行时间超过310毫秒就不用睡了
                    if(useTime < 310){
                        // 限制一分钟不超过200次
                        Thread.sleep(310 - useTime);
                    }
                } catch (Exception e) {
                    log.error("同步第{}个商品scenicId={}异常（只更新本地已有商品），", i, id, e);
                }
                i++;
            }
            log.info("同步驴妈妈商品定时任务执行完成（只更新本地已有商品），共{}个，用时{}秒（只更新本地已有商品）", i, (System.currentTimeMillis() - begin) / 1000);
        } catch (Exception e) {
            log.error("执行驴妈妈定时更新商品任务异常（只更新本地已有商品）", e);
        }
    }

    /**
     * 驴妈妈一周更新一次，周一凌晨1点
     */
//    @Scheduled(cron = "0 0 1 ? * MON")
    public void syncNewProduct(){
        try {
            if(schedule == null || !StringUtils.equalsIgnoreCase("yes", schedule)){
                return;
            }
            long begin = System.currentTimeMillis();
            log.info("开始执行定时任务，同步驴妈妈商品。。(拉取本地没有的商品)");
            LmmProductListRequest request = new LmmProductListRequest();
            request.setCurrentPage(1);
            while (true){
                long sTime = System.currentTimeMillis();
                // 驴妈妈不区分新增和更新，一起执行
                boolean success = lmmScenicService.syncProductList(request, PRODUCT_SYNC_MODE_UNLIMITED);
                long useTime = System.currentTimeMillis() - sTime;
                log.info("同步第{}页商品，用时{}毫秒，(拉取本地没有的商品)", request.getCurrentPage(), useTime);
                if(!success) {
                    break;
                }
                request.setCurrentPage(request.getCurrentPage() + 1);
                // 如果执行时间超过310毫秒就不用睡了
                if(useTime < 310){
                    // 限制一分钟不超过200次
                    Thread.sleep(310 - useTime);
                }
            }
            log.info("同步驴妈妈商品定时任务执行完成，共同步{}页，用时{}秒，(拉取本地没有的商品)", request.getCurrentPage(), (System.currentTimeMillis() - begin) / 1000);
        } catch (Exception e) {
            log.error("执行驴妈妈定时更新商品任务异常，(拉取本地没有的商品)", e);
        }
    }

    /**
     * 只更新本地已有景点
     */
//    @Scheduled(cron = "0 0 1 ? * SUN")
    public void syncUpdateScenicV2(){
        try {
//            if(schedule == null || !StringUtils.equalsIgnoreCase("yes", schedule)){
//                return;
//            }
            long begin = System.currentTimeMillis();
            log.info("开始执行定时任务v2，同步驴妈妈景点（只更新本地已有景点）。。");
            List<String> ids = lmmScenicService.getSupplierScenicIdsV2();
            if(ListUtils.isEmpty(ids)){
                log.error("同步驴妈妈景点定时任务执行完成v2（只更新本地已有景点），没有找到驴妈妈景点。");
                return;
            }
            int i = 1;
            for (String id : ids) {
                try {
                    long sTime = System.currentTimeMillis();
                    lmmScenicService.syncScenicListByIdV2(id);
                    long useTime = System.currentTimeMillis() - sTime;
                    log.info("同步第{}个景点v2 scenicId={}，用时{}毫秒（只更新本地已有景点）",
                            i, id, useTime);
                    if(useTime < 10){
                        Thread.sleep(10 - useTime);
                    }
                } catch (Exception e) {
                    log.error("同步第{}个景点scenicId={}异常v2（只更新本地已有景点），", i, id, e);
                }
                i++;
            }
            log.info("同步驴妈妈景点定时任务执行完成v2（只更新本地已有景点），共{}个，用时{}秒（只更新本地已有景点）", i, (System.currentTimeMillis() - begin) / 1000);
        } catch (Exception e) {
            log.error("执行驴妈妈定时更新景点任务异常v2（只更新本地已有景点）", e);
        }
    }

    /**
     * 只更新本地已有商品，驴妈妈全量同步所有，不用区分已有还是没有
     */
//    @Scheduled(cron = "0 0 1 ? * SUN")
    public void syncUpdateProductV2(){
        try {
//            if(schedule == null || !StringUtils.equalsIgnoreCase("yes", schedule)){
//                return;
//            }
            long begin = System.currentTimeMillis();
            log.info("开始执行定时任务，同步驴妈妈商品v2（只更新本地已有商品）。。");
            List<String> ids = lmmScenicService.getSupplierProductIdsV2();
            if(ListUtils.isEmpty(ids)){
                log.error("同步驴妈妈商品定时任务执行完成v2（只更新本地已有商品），没有找到驴妈妈商品。");
                return;
            }
            int i = 1;
            for (String id : ids) {
                try {
                    long sTime = System.currentTimeMillis();
                    lmmScenicService.syncGoodsListByIdV2(id);
                    long useTime = System.currentTimeMillis() - sTime;
                    log.info("同步第{}个商品v2 scenicId={}，用时{}毫秒（只更新本地已有商品）",
                            i, id, useTime);
                    if(useTime < 10){
                        Thread.sleep(10 - useTime);
                    }
                } catch (Exception e) {
                    log.error("同步第{}个商品scenicId={}异常v2（只更新本地已有商品），", i, id, e);
                }
                i++;
            }
            log.info("同步驴妈妈商品定时任务执行完成v2（只更新本地已有商品），共{}个，用时{}秒（只更新本地已有商品）", i, (System.currentTimeMillis() - begin) / 1000);
        } catch (Exception e) {
            log.error("执行驴妈妈定时更新商品任务异常v2（只更新本地已有商品）", e);
        }
    }

    /**
     * 驴妈妈一周更新一次，周一凌晨1点
     */
    @Scheduled(cron = "0 0 1 ? * MON")
    public void syncAllProductV2(){
        try {
            if(schedule == null || !StringUtils.equalsIgnoreCase("yes", schedule)){
                return;
            }
            long begin = System.currentTimeMillis();
            log.info("开始执行定时任务，同步驴妈妈商品v2。。(拉取本地没有的商品)");
            LmmProductListRequest request = new LmmProductListRequest();
            request.setCurrentPage(1);
            while (true){
                long sTime = System.currentTimeMillis();
                // 驴妈妈不区分新增和更新，一起执行
                boolean success = lmmScenicService.syncProductListV2(request);
                long useTime = System.currentTimeMillis() - sTime;
                log.info("同步第{}页商品v2，用时{}毫秒，(拉取本地没有的商品)", request.getCurrentPage(), useTime);
                if(!success) {
                    break;
                }
                request.setCurrentPage(request.getCurrentPage() + 1);
                if(useTime < 10){
                    Thread.sleep(10 - useTime);
                }
            }
            log.info("同步驴妈妈商品定时任务执行完成v2，共同步{}页，用时{}秒，(拉取本地没有的商品)", request.getCurrentPage(), (System.currentTimeMillis() - begin) / 1000);
        } catch (Exception e) {
            log.error("执行驴妈妈定时更新商品任务异常v2，(拉取本地没有的商品)", e);
        }
    }

    /**
     * 驴妈妈一周更新一次，周二凌晨2点（周一老版更新）
     */
    @Scheduled(cron = "0 0 2 ? * 3")
    @Async
    public void syncNewProductV2(){
        try {
            if(schedule == null || !StringUtils.equalsIgnoreCase("yes", schedule)){
                return;
            }
            long begin = System.currentTimeMillis();
            log.info("开始执行定时任务，同步驴妈妈产品v2。。");
            LmmProductListRequest request = new LmmProductListRequest();
            request.setCurrentPage(1);
            while (true){
                long sTime = System.currentTimeMillis();
                // 驴妈妈不区分新增和更新，一起执行
                boolean success = lmmScenicService.syncProductListV2(request);
                long useTime = System.currentTimeMillis() - sTime;
                log.info("同步第{}页产品v2，用时{}毫秒", request.getCurrentPage(), useTime);
                if(!success) {
                    break;
                }
                request.setCurrentPage(request.getCurrentPage() + 1);
                // 如果执行时间超过310毫秒就不用睡了
                if(useTime < 10){
                    // 限制一分钟不超过200次
                    Thread.sleep(10 - useTime);
                }
            }
            log.info("同步驴妈妈产品定时任务执行完成v2，共同步{}页，用时{}秒", request.getCurrentPage(), (System.currentTimeMillis() - begin) / 1000);
        } catch (Exception e) {
            log.error("执行驴妈妈定时更新产品任务异常v2", e);
        }
    }

    // 这里前期为了落基础数据，后面不需要主动获取
//    @Scheduled(cron = "0 0 1 ? * *")
    @Async
    public void syncScenicAll(){
        try {
            if(schedule == null || !StringUtils.equalsIgnoreCase("yes", schedule)){
                return;
            }
            long begin = System.currentTimeMillis();
            log.info("开始执行定时任务，同步驴妈妈景点（分页同步）。。");
            LmmScenicListRequest request = new LmmScenicListRequest();
            int i = 1;
            while (true) {
                try {
                    request.setCurrentPage(i);
                    long sTime = System.currentTimeMillis();
                    boolean b = lmmScenicService.syncScenicListV2(request);
                    if(!b){
                        break;
                    }
                    long useTime = System.currentTimeMillis() - sTime;
                    log.info("同步第{}页景点 ，用时{}毫秒（分页同步）",
                            i, useTime);
                    // 如果执行时间超过310毫秒就不用睡了
                    if(useTime < 310){
                        // 限制一分钟不超过200次
                        Thread.sleep(310 - useTime);
                    }
                } catch (Exception e) {
                    log.error("同步第{}页景点异常（分页同步），", i, e);
                }
                i++;
            }
            log.info("同步驴妈妈景点定时任务执行完成（分页同步），共{}页，用时{}秒", i, (System.currentTimeMillis() - begin) / 1000);
        } catch (Exception e) {
            log.error("执行驴妈妈定时更新景点任务异常（分页同步）", e);
        }
    }
}
