package com.huoli.trip.supplier.self.yaochufa.vo.push;

import com.huoli.trip.supplier.self.yaochufa.vo.YcfVoucher;
import lombok.Data;

import java.util.List;

/**
 * 描述: <br> 接收订单状态推送结果
 * 版权：Copyright (c) 2011-2020<br>
 * 公司：活力天汇<br>
 * 作者：王德铭<br>
 * 版本：1.0<br>
 * 创建日期：2020/6/19<br>
 */
@Data
public class OrderStatusInfo {
    //【合】订单号
    private String partnerOrderId;
    //订单状态
    private int orderStatus;
    //电子凭证码(当订单含有票资源，且该票资源存在凭证码时，订单状态为待出行“20”时推送此值)
    private List<YcfVoucher> vochers;
    //备注（取消订单的原因等描述信息）
    private String remark;
}
