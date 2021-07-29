package com.huoli.trip.supplier.web.task;


import com.huoli.trip.common.util.UploadUtil;
import com.huoli.trip.supplier.web.service.CommonService;
import com.huoli.trip.supplier.web.util.SpringContextProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * @description: 处理远程图片下载到本地定时任务
 * @author: linxiaopeng
 * @create: 2021-07-27 10:13
 **/
@Slf4j
@Component
public class UploadImageToLocalTask {

    @Scheduled(cron = "0 0 0 1 1/1 ?")
    public void uploadImageToLocal(){
        CommonService commonService = SpringContextProvider.getBean(CommonService.class);
        commonService.upLoadImageToLocal(null);
    }

}