package com.huoli.trip.supplier.web.difengyun.task;

import com.huoli.trip.supplier.self.difengyun.vo.request.DfyScenicListRequest;
import com.huoli.trip.supplier.web.difengyun.service.DfySyncService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

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

    @Scheduled(cron = "0 0 6-12 ? * *")
    public void syncFullPrice(){
        if(schedule == null || !StringUtils.equalsIgnoreCase("yes", schedule)){
            return;
        }
        long begin = System.currentTimeMillis();
        log.info("开始执行定时任务，同步要出发价格日历。。");
        DfyScenicListRequest request = new DfyScenicListRequest();
        request.setPage(1);
        request.setPageSize(100);
        // todo 这里需要确认是否能一次大量获取
        while (dfySyncService.syncScenicList(request)){
            request.setPage(request.getPage() + 1);
        }
        log.info("定时任务执行完成，用时{}秒", (System.currentTimeMillis() - begin) / 1000);
    }
}
