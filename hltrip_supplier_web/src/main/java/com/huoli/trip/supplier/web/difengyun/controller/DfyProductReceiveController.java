package com.huoli.trip.supplier.web.difengyun.controller;

import com.huoli.trip.supplier.self.difengyun.vo.request.DfyBaseRequest;
import com.huoli.trip.supplier.self.difengyun.vo.request.DfyProductNoticeRequest;
import com.huoli.trip.supplier.self.difengyun.vo.response.DfyBaseResult;
import com.huoli.trip.supplier.web.difengyun.service.DfySyncService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 描述：<br/>
 * 版权：Copyright (c) 2011-2020<br>
 * 公司：活力天汇<br>
 * 作者：冯志强<br>
 * 版本：1.0<br>
 * 创建日期：2020/12/11<br>
 */
@RestController
@Slf4j
@RequestMapping(value = "/dfy/receive")
public class DfyProductReceiveController {

    @Autowired
    private DfySyncService dfySyncService;

    /**
     * 接收产品更新通知（只有跟团游，门票没有通知）
     * @param request
     * @return
     */
    @PostMapping(path = "/product")
    DfyBaseResult dfyReceiveProduct(@RequestBody DfyBaseRequest<DfyProductNoticeRequest> request) {
//        dfySyncService.productUpdate(request.getData());
        dfySyncService.productUpdateV2(request.getData());
        return DfyBaseResult.success();
    }
}
