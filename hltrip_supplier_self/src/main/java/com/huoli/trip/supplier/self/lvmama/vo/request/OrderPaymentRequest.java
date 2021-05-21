package com.huoli.trip.supplier.self.lvmama.vo.request;

import com.huoli.trip.supplier.self.lvmama.vo.OrderPaymentInfo;
import lombok.Data;

/**
 * @author lunatic
 * @Title:
 * @Package
 * @Description:
 * @date 2021/3/1517:28
 */
@Data
public class OrderPaymentRequest  extends LmmBaseRequest{
    private OrderPaymentInfo order;
}
