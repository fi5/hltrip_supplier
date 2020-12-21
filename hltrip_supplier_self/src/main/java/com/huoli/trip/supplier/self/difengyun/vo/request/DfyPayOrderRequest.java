package com.huoli.trip.supplier.self.difengyun.vo.request;

import com.huoli.trip.common.vo.request.TraceRequest;
import lombok.Data;

import java.io.Serializable;

/**
 * @author lunatic
 * @Title:
 * @Package
 * @Description:
 * @date 2020/12/1016:28
 */
@Data
public class DfyPayOrderRequest extends TraceRequest implements Serializable {
    private static final long serialVersionUID = 4299309895979424744L;
    private String channelCode;
    private String channelOrderId;
    private String price;
}
