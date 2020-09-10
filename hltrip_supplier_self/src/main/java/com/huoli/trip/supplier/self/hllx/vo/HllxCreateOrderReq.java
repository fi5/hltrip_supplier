package com.huoli.trip.supplier.self.hllx.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class HllxCreateOrderReq implements Serializable {
    private String productId;
    private int qunatity;
    private String date;
}
