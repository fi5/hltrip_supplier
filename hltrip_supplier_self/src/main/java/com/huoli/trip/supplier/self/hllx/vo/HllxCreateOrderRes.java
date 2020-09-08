package com.huoli.trip.supplier.self.hllx.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class HllxCreateOrderRes implements Serializable {
    //订单状态
    private int orderStatus;
    //【要】订单编号
    private String orderId;
}
