package com.huoli.trip.supplier.web.hllx.controller;


import com.huoli.trip.common.vo.TripNotice;
import com.huoli.trip.common.vo.response.BaseResponse;
import com.huoli.trip.supplier.self.hllx.vo.HllxOrderOperationRequest;
import com.huoli.trip.supplier.self.yaochufa.vo.basevo.YcfBaseResult;
import com.huoli.trip.supplier.web.hllx.service.HllxSyncService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping(value = {"/api/service/hltrip"})
@Slf4j
public class HllxController {
    @Autowired
    HllxSyncService hllxSyncService;

    @PostMapping(path = "/pushOrderStatus")
    BaseResponse<Boolean> pushOrderStatus(@RequestBody HllxOrderOperationRequest request) {
        try{
            hllxSyncService.getOrderStatus(request);
            return  BaseResponse.withSuccess(true);
        }catch (Exception e){
            return  BaseResponse.withFail(1,"推送信息失败");
        }
    }

    @PostMapping(path = "/tripNotice")
    BaseResponse<Boolean> pushOrderStatus(@RequestBody TripNotice request) {
        try{
            hllxSyncService.tripNotice(request);
            return  BaseResponse.withSuccess(true);
        }catch (Exception e){
            log.error("处理出团通知异常", e);
            return  BaseResponse.withFail(1,"处理出团通知失败");
        }
    }

}
