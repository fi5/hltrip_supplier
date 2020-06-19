package com.huoli.trip.supplier.self.yaochufa.vo;

import java.io.Serializable;
import java.util.List;

public class YcfOrderStatusResult implements Serializable {
    private String  orderId;
    private int code;
    private List<YcfVocher> ycfVochers;
}
