package com.huoli.trip.supplier.self.universal.vo.reqeust;

import com.huoli.trip.common.vo.request.TraceRequest;
import com.huoli.trip.supplier.self.universal.vo.UBRGuest;
import com.huoli.trip.supplier.self.universal.vo.UBRTicketEntity;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 描述：<br/>
 * 版权：Copyright (c) 2011-2020<br>
 * 公司：活力天汇<br>
 * 作者：冯志强<br>
 * 版本：1.0<br>
 * 创建日期：2021/8/3<br>
 */
@Data
public class UBRTicketOrderRequest extends TraceRequest implements Serializable {

    /**
     * 出行人
     */
    private List<UBRTicketEntity> ticketEntity;

    /**
     * 联系人
     */
    private UBRGuest contact;
}
