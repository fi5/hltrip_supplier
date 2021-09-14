package com.huoli.trip.supplier.api;

import com.huoli.trip.supplier.self.universal.vo.UBRTicketList;
import com.huoli.trip.supplier.self.universal.vo.UBRVirtualStock;
import com.huoli.trip.supplier.self.universal.vo.reqeust.UBRStockRequest;
import com.huoli.trip.supplier.self.universal.vo.reqeust.UBRTicketListRequest;

import java.util.List;

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

    /**
     * 登录
     * @return
     */
    String getToken();

    /**
     * 刷新token
     * @return
     */
    String refreshToken();

    /**
     * 检查登录情况
     */
    void checkUserInfo();

    /**
     * 同步产品
     * @param type
     */
    void syncProduct(String type);

    /**
     * 同步库存
     * @param request
     * @return
     */
    List<UBRVirtualStock> getStock(UBRStockRequest request);

    /**
     * 同步库存
     * @param startDate
     * @param endDate
     * @return
     */
    List<UBRVirtualStock> getStock(String startDate, String endDate);

    /**
     * 这个主要为了同步库存
     * @return
     */
    UBRTicketList getTicketList();
}
