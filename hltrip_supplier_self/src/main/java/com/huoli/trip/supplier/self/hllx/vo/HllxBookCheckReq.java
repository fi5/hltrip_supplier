package com.huoli.trip.supplier.self.hllx.vo;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
public class HllxBookCheckReq  implements Serializable {
    //产品编号
    private String productId;
    //开始日期
    @NotNull(message = "开始日期为空")
    private String beginDate;
    //结束日期
    @NotNull(message = "结束日期为空")
    private String endDate;
}