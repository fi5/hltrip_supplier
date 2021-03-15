package com.huoli.trip.supplier.self.lvmama.vo.request;

import com.huoli.trip.supplier.self.lvmama.vo.OrderInfo;
import com.huoli.trip.supplier.self.lvmama.vo.Recipient;
import com.huoli.trip.supplier.self.lvmama.vo.Traveller;
import lombok.Data;

import java.awt.print.Book;
import java.util.List;

/**
 * @author lunatic
 * @Title:
 * @Package
 * @Description:
 * @date 2021/3/1517:21
 */
@Data
public class CreateOrderRequest {
    private OrderInfo orderInfo;
    private Book book;
    private List<Traveller> traveller;
    private Recipient recipient;
}
