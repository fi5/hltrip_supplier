package com.huoli.trip.supplier.self.hllx.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class HllxCancelOrderRes implements Serializable {
    private int orderStatus;

    public HllxCancelOrderRes() {
    }

    public HllxCancelOrderRes(int orderStatus) {
        this.orderStatus = orderStatus;
    }
}
