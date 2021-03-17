package com.huoli.trip.supplier.self.lvmama.vo.response;

import com.huoli.trip.supplier.self.lvmama.vo.CreateOrderInfo;
import lombok.Data;

/**
 * @author lunatic
 * @Title:
 * @Package
 * @Description:
 * @date 2021/3/1517:24
 */
@Data
public class OrderResponse extends  LmmBaseResponse{
    private CreateOrderInfo order;
}
