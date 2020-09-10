package com.huoli.trip.supplier.self.hllx.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class HllxCancelOrderReq implements Serializable {
    private String partnerOrderId;
    private String remark;
}
