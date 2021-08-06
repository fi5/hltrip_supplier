package com.huoli.trip.supplier.web.mapper;

import com.huoli.trip.common.entity.TripOrder;
import com.huoli.trip.common.entity.TripOrderVoucher;
import com.huoli.trip.common.entity.TripPayOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Mapper
public interface TripOrderMapper {
    @Select("SELECT channelStatus,quantity,childQuantity,beginDate,endDate,productCode,productId FROM trip_order WHERE orderId = #{orderId}")
    TripOrder getOrderStatusByOrderId(@Param("orderId") String orderId);

    @Select("select * from trip_order_voucher where orderId = #{orderId}")
    List<TripOrderVoucher> getVoucherInfoByOrderId(String orderId);

    @Select("select * from trip_order where orderId = #{orderId}")
    TripOrder getChannelByOrderId(String orderId);

    @Select("select * from trip_pay_order where orderId = #{orderId}")
    List<TripPayOrder> getOrderPayList(String orderId);

    @Select("select * from trip_order where outOrderId = #{outOrderId}")
    TripOrder getOrderByOutOrderId(String outOrderId);

    @Select("SELECT extend FROM trip_order WHERE orderId = #{orderId}")
    String getExtendById(String orderId);

    @Update("update trip_order set extend = #{extend} WHERE orderId = #{orderId}")
    void updateExtendById(@Param("orderId") String orderId, @Param("extend") String extend);

    @Select("select outOrderId, outPayPrice, extend, status, channelStatus from trip_order where orderId = #{orderId}")
    TripOrder getOrderByOrderId(String orderId);

    @Update("update trip_order set outOrderId = #{outOrderId} WHERE orderId = #{orderId}")
    void updateOutOrderIdById(@Param("orderId") String orderId, @Param("channelStatus") int channelStatus, @Param("outOrderId") String outOrderId);
}
