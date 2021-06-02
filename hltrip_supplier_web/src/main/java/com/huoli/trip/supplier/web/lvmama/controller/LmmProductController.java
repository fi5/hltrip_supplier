package com.huoli.trip.supplier.web.lvmama.controller;

import com.alibaba.fastjson.JSONObject;
import com.huoli.trip.supplier.self.lvmama.vo.push.LmmProductPushRequest;
import com.huoli.trip.supplier.self.lvmama.vo.push.LmmRefundPushRequest;
import com.huoli.trip.supplier.self.lvmama.vo.response.LmmBaseResponse;
import com.huoli.trip.supplier.web.lvmama.service.LmmSyncService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 描述：<br/>
 * 版权：Copyright (c) 2011-2020<br>
 * 公司：活力天汇<br>
 * 作者：冯志强<br>
 * 版本：1.0<br>
 * 创建日期：2021/5/10<br>
 */
@RestController
@Slf4j
@RequestMapping(value = "/lmm/product")
public class LmmProductController {

    @Autowired
    private LmmSyncService lmmSyncService;

    @PostMapping(path = "/pushProductChangeInfo")
    public LmmBaseResponse productUpdate(@RequestParam("product") String product) {
        try {
            lmmSyncService.pushUpdateV2(product);
        } catch (Exception e) {
            log.error("接收驴妈妈产品通知异常v2", e);
            return LmmBaseResponse.fail();
        }
        try{
            lmmSyncService.pushUpdate(product);
        } catch (Exception e){
            log.error("接收驴妈妈产品通知异常", e);
            return LmmBaseResponse.fail();
        }
        return LmmBaseResponse.success();
    }
}
