package com.huoli.trip.supplier.self.lvmama.vo.request;

import com.huoli.trip.supplier.self.lvmama.vo.Booker;
import com.huoli.trip.supplier.self.lvmama.vo.OrderInfo;
import com.huoli.trip.supplier.self.lvmama.vo.Recipient;
import com.huoli.trip.supplier.self.lvmama.vo.Traveller;
import lombok.Data;

import java.util.List;

/**
 * @author lunatic
 * @Title:
 * @Package
 * @Description:
 * @date 2021/3/1514:55
 */
@Data
public class ValidateOrderRequest  extends LmmBaseRequest {
    private OrderInfo orderInfo;

}
