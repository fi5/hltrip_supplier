package com.huoli.trip.supplier.web.mapper;

import com.huoli.trip.common.entity.TripOrder;
import com.huoli.trip.common.entity.TripOrderVoucher;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Mapper
public interface TripOrderMapper {
    @Select("SELECT channelStatus,quantity,childQuantity,beginDate,endDate,productCode,productId FROM trip_order WHERE orderId = #{orderId}")
    TripOrder getOrderStatusByOrderId(@Param("orderId") String orderId);

    @Select("select * from trip_order_voucher where orderId = #{orderId}")
    List<TripOrderVoucher> getVoucherInfoByOrderId(String orderId);
}
