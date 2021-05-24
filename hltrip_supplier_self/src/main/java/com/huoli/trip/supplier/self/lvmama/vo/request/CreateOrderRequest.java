package com.huoli.trip.supplier.self.lvmama.vo.request;


import com.huoli.trip.supplier.self.lvmama.vo.OrderInfo;
import lombok.Data;


/**
 * @author lunatic
 * @Title:
 * @Package
 * @Description:
 * @date 2021/3/1517:21
 */
@Data
public class CreateOrderRequest extends LmmBaseRequest {
    private OrderInfo orderInfo;

}
