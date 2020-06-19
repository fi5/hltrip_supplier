package com.huoli.trip.common.entity;

import java.io.Serializable;
import java.util.List;

public class OrderStatusResult implements Serializable {
    private String  orderId;
    private int code;
    private List<Vocher> vochers;
}
