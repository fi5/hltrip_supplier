package com.huoli.trip.supplier.self.yaochufa.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class YcfOrderStatusResult implements Serializable {
    private String  orderId;
    private int orderStatus;
    private List<YcfVoucher> vochers;
}
