package com.huoli.trip.supplier.self.difengyun.vo.request;

import com.huoli.trip.common.vo.request.TraceRequest;
import lombok.Data;

import java.io.Serializable;

/**
 * @author lunatic
 * @Title:
 * @Package
 * @Description: 取消订单 返回结果业务参数为空,即data内容没有意义,如有值请忽略,请使用订单状态接口查询订单状态.
 * @date 2020/12/1011:22
 */
@Data
public class DfyCancelOrderRequest extends TraceRequest implements Serializable {
    private static final long serialVersionUID = -136855836677820269L;
    private String orderId;
    private String remark;
}
