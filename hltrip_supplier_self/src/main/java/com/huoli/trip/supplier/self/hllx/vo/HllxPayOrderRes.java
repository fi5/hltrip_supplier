package com.huoli.trip.supplier.self.hllx.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class HllxPayOrderRes implements Serializable {
    //订单状态
    private int orderStatus;

    public HllxPayOrderRes() {
    }

    public HllxPayOrderRes(int orderStatus) {
        this.orderStatus = orderStatus;
    }
}

