package com.huoli.trip.supplier.self.difengyun.vo.request;

import com.huoli.trip.common.vo.request.TraceRequest;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @author lunatic
 * @Title:
 * @Package
 * @Description:
 * @date 2020/12/1016:24
 */
@Data
public class DfyBookCheckRequest extends TraceRequest implements Serializable {
    //产品编号
    private String productId;
    /**
     * 套餐ID
     */
    private String packageid;
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