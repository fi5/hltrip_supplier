package com.huoli.trip.supplier.self.difengyun.vo.request;

import lombok.Data;

import java.io.Serializable;

/**
 * @author lunatic
 * @Title:
 * @Package
 * @Description:退票申请请求参数对象
 * @date 2020/12/1016:09
 */
@Data
public class DfyRefundTicketRequest implements Serializable {
    private static final long serialVersionUID = -1629752195634863484L;
    private String orderId;
    /**
     * 退订类型
     * 0	行程变更
     * 1	价格不优惠
     * 2	门票预订错误
     * 3	未收到入园凭证
     * 4	景区闭园
     * 5	其他原因（传5的时候，content自填，最好不要超过50字）
     */
    private String causeType;
    /**
     * 退订原因
     */
    private String causeContent;
}
