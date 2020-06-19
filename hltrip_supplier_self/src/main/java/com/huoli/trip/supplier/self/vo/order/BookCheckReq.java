package com.huoli.trip.supplier.self.vo.order;

import lombok.Data;

import java.util.Date;

/**
 * 描述: <br>可预订检查请求
 * 版权：Copyright (c) 2011-2020<br>
 * 公司：活力天汇<br>
 * 作者：王德铭<br>
 * 版本：1.0<br>
 * 创建日期：2020/6/18<br>
 */
@Data
public class BookCheckReq{
    //产品编号
    private String productId;
    //开始日期
    private Date beginDate;
    //结束日期
    private Date endDate;

}
