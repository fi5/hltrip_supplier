package com.huoli.trip.supplier.web.universal.controller;

import com.huoli.trip.supplier.self.difengyun.vo.response.DfyBaseResult;
import com.huoli.trip.supplier.self.lvmama.vo.request.LmmScenicListRequest;
import com.huoli.trip.supplier.web.lvmama.service.LmmSyncService;
import com.huoli.trip.supplier.web.lvmama.task.LmmTicketTask;
import com.huoli.trip.supplier.web.universal.service.UBRProductService;
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
 * 创建日期：2021/5/17<br>
 */
@RestController
@Slf4j
@RequestMapping(value = "/ubr/test")
public class UBRTestController {

    @Autowired
    private UBRProductService ubrProductService;

    @PostMapping(path = "/init")
    public DfyBaseResult UBRInit() {
        ubrProductService.init();
        return DfyBaseResult.success();
    }

}
