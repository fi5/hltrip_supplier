package com.huoli.trip.supplier.self.hllx.vo;

import com.huoli.trip.common.vo.request.TraceRequest;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class HllxBookCheckReq  extends TraceRequest {
    //产品编号
    private String productId;
    /**
     * 套餐ID
     */
    private String packageId;
    /**
     * 类目
     */
    private String category;
    //开始日期
    @NotNull(message = "开始日期为空")
    private String beginDate;
    //结束日期
    @NotNull(message = "结束日期为空")
    private String endDate;
}