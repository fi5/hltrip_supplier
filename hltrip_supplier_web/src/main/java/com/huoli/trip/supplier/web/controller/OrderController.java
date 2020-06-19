package com.huoli.trip.supplier.web.controller;

import com.huoli.trip.supplier.feign.clinet.yaochufa.IYaoChuFaClient;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import com.huoli.trip.supplier.self.vo.basevo.BaseRequest;
import com.huoli.trip.supplier.self.vo.basevo.BaseResponse;
import com.huoli.trip.supplier.self.vo.order.BookCheckReq;
import com.huoli.trip.supplier.self.vo.order.BookCheckRes;


/**
 * 描述: <br>订单
 * 版权：Copyright (c) 2011-2020<br>
 * 公司：活力天汇<br>
 * 作者：王德铭<br>
 * 版本：1.0<br>
 * 创建日期：2020/6/18<br>
 */
@RestController
@Api(description = "订单")
public class OrderController {
    @Autowired
    private IYaoChuFaClient iYaoChuFaClient;
    @ApiOperation("可预订检查服务")
    @PostMapping(path = "/getCheckInfos")
    BaseResponse<BookCheckRes> getCheckInfos(@RequestBody BaseRequest<BookCheckReq> req) {
        return iYaoChuFaClient.getCheckInfos(req);
    }
}
