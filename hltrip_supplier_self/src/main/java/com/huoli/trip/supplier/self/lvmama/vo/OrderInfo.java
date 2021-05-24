package com.huoli.trip.supplier.self.lvmama.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author lunatic
 * @Title:
 * @Package
 * @Description:
 * @date 2021/3/1515:10
 */
@Data
public class OrderInfo implements Serializable {
    /**
     * yes
     * 分销订单编号
     */
    private String partnerOrderNo;
    /**
     * yes
     * 订单总金额
     */
    private String orderAmount;
    /**
     * no
     * 场次id 指定场次 id（分销商指定场次下单必传）
     */
    private String seasonId;

    private Product product;

    private Booker booker;
    private List<Traveller> traveller;
    private Recipient recipient;

    public OrderInfo() {
    }

    public OrderInfo(String partnerOrderNo, String orderAmount, String seasonId) {
        this.partnerOrderNo = partnerOrderNo;
        this.orderAmount = orderAmount;
        this.seasonId = seasonId;
    }
}
