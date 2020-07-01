package com.huoli.trip.supplier.self.yaochufa.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class YcfVouchersResult implements Serializable {
    List<YcfVoucher> vochers;
}
