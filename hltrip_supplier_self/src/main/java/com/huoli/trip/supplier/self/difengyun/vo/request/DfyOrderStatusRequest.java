package com.huoli.trip.supplier.self.difengyun.vo.request;

import lombok.Data;

import java.io.Serializable;

/**
 * @author lunatic
 * @Title:
 * @Package
 * @Description:订单状态获取请求参数
 * @date 2020/12/1015:15
 */
@Data
public class DfyOrderStatusRequest implements Serializable {
    private static final long serialVersionUID = -675770895456085173L;
    private String orderId;
    private String acctId;
}
