package com.huoli.trip.supplier.web.universal.service;

import com.huoli.trip.supplier.self.universal.vo.UBRTicketList;
import com.huoli.trip.supplier.self.universal.vo.reqeust.UBRTicketListRequest;

/**
 * 描述：<br/>
 * 版权：Copyright (c) 2011-2020<br>
 * 公司：活力天汇<br>
 * 作者：冯志强<br>
 * 版本：1.0<br>
 * 创建日期：2021/7/28<br>
 */
public interface UBRProductService {

    /**
     * 门票列表
     * @param request
     * @return
     */
    UBRTicketList getTicketList(UBRTicketListRequest request);

    /**
     * 初始化字典
     */
    void init();
}
