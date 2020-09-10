package com.huoli.trip.supplier.self.hllx.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class HllxOrderStatusResult implements Serializable {
    private String  orderId;
    private int orderStatus;
}
