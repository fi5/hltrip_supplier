package com.huoli.trip.supplier.web.hllx.controller;


import com.huoli.trip.supplier.self.hllx.vo.HllxBaseResult;
import com.huoli.trip.supplier.self.hllx.vo.HllxOrderOperationRequest;
import com.huoli.trip.supplier.web.hllx.service.HllxSyncService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(value = {"/api/service/hltrip/order"})
@Slf4j
public class HllxController {
    @Autowired
    HllxSyncService hllxSyncService;

    @PostMapping(path = "/pushOrderStatus")
    HllxBaseResult<Boolean> pushOrderStatus(@RequestBody HllxOrderOperationRequest request) {
        try{
            hllxSyncService.getOrderStatus(request);
        }catch (Exception e){
            return new HllxBaseResult(true,200,true);
        }
        return new HllxBaseResult(true,200,false);
    }

}
