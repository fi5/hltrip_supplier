package com.huoli.trip.supplier.self.hllx.vo;

import com.huoli.trip.common.vo.response.order.OrderDetailRep;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class HllxOrderVoucherResult implements Serializable {
    private String  orderId;//渠道订单id
    private int orderStatus;
    //0	待支付：创建订单成功，合作方尚未付款。
//	10	待确认：支付订单成功，要出发确认流程中
//	11	待确认（申请取消）：合作方申请取消，要出发在审核状态
//	12	[全网预售特有]预约出行：待二次预约
//	13	[全网预售特有]立即补款：待二次预约补款
//	20	待出行：要出发已确认订单，客人可出行消费
//	30	已消费：客人已消费订单
//	40	已取消：订单已取消
    private List<OrderDetailRep.Voucher> vochers;
}
