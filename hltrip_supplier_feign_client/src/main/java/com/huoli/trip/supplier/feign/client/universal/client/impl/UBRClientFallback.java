package com.huoli.trip.supplier.feign.client.universal.client.impl;

import com.huoli.trip.supplier.feign.client.universal.client.IUBRClient;
import com.huoli.trip.supplier.self.universal.vo.UBRTicketList;
import com.huoli.trip.supplier.self.universal.vo.reqeust.UBRLoginRequest;
import com.huoli.trip.supplier.self.universal.vo.reqeust.UBRTicketListRequest;
import com.huoli.trip.supplier.self.universal.vo.response.UBRBaseResponse;
import com.huoli.trip.supplier.self.universal.vo.response.UBRLoginResponse;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;


/**
 * 描述：<br/>
 * 版权：Copyright (c) 2011-2020<br>
 * 公司：活力天汇<br>
 * 作者：冯志强<br>
 * 版本：1.0<br>
 * 创建日期：2020/12/8<br>
 */
@Slf4j
@Component
public class UBRClientFallback implements FallbackFactory<IUBRClient> {

    @Override
    public IUBRClient create(Throwable throwable) {
        String msg = throwable == null ? "" : throwable.getMessage();
        if (!StringUtils.isEmpty(msg)) {
            log.error(msg);
        }
        return new IUBRClient() {
            @Override
            public UBRBaseResponse<UBRLoginResponse> login(@RequestBody UBRLoginRequest request) {
                return null;
            }

            @Override
            public UBRBaseResponse<UBRLoginResponse> refreshToken() {
                return null;
            }

            @Override
            public UBRBaseResponse<UBRTicketList> getTicketList(UBRTicketListRequest request) {
                return null;
            }

            @Override
            public UBRBaseResponse init() {
                return null;
            }
        };

    }
}
