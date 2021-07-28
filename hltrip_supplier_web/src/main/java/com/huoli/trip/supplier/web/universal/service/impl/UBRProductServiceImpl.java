package com.huoli.trip.supplier.web.universal.service.impl;

import com.huoli.trip.common.vo.response.BaseResponse;
import com.huoli.trip.supplier.feign.client.universal.client.IUBRClient;
import com.huoli.trip.supplier.self.universal.vo.UBRTicketList;
import com.huoli.trip.supplier.self.universal.vo.reqeust.UBRTicketListRequest;
import com.huoli.trip.supplier.self.universal.vo.response.UBRBaseResponse;
import com.huoli.trip.supplier.web.universal.service.UBRProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 描述：<br/>
 * 版权：Copyright (c) 2011-2020<br>
 * 公司：活力天汇<br>
 * 作者：冯志强<br>
 * 版本：1.0<br>
 * 创建日期：2021/7/28<br>
 */
@Service
@Slf4j
public class UBRProductServiceImpl implements UBRProductService {

    @Autowired
    private IUBRClient ubrClient;

    public UBRTicketList getTicketList(UBRTicketListRequest request){
        UBRBaseResponse<UBRTicketList> response = ubrClient.getTicketList(request);
        if(response == null){
            log.error("环球影城门票列表无返回内容");
            return null;
        }
        if(response.getCode() != 200){
            log.error("环球影城门票列表返回失败，code={}, msg={}", response.getCode(), response.getMsg());
            return null;
        }
        if(response.getData() == null){
            log.error("环球影城门票列表返回空数据");
            return null;
        }
        return response.getData();
    }

    public void init(){
        UBRBaseResponse response = ubrClient.init();
        if(response == null){
            log.error("环球影城初始化无返回内容");
        }
        if(response.getCode() != 200){
            log.error("环球影城初始化返回失败，code={}, msg={}", response.getCode(), response.getMsg());
        }
        if(response.getData() == null){
            log.error("环球影城初始化返回空数据");
        }
    }

    public void syncProduct(String type){
        UBRTicketListRequest request = new UBRTicketListRequest();
        request.setType(type);
        UBRTicketList ubrTicketList = getTicketList(request);

    }
}
