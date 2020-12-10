package com.huoli.trip.supplier.self.difengyun.vo.response;

import lombok.Data;

import java.io.Serializable;

/**
 * @author lunatic
 * @Title:
 * @Package
 * @Description:订单状态结果
 * @date 2020/12/1015:17
 */
@Data
public class DfyOrderStatusResponse implements Serializable {
    private static final long serialVersionUID = 6879632982474481696L;
    private String orderStatus;
}
