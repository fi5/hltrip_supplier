package com.huoli.trip.supplier.web.mapper;

import com.huoli.trip.common.entity.TripOrder;
import com.huoli.trip.common.entity.TripOrderRefund;
import com.huoli.trip.common.entity.TripOrderVoucher;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Mapper
public interface TripOrderRefundMapper {

    @Select("select * from trip_order_refund where orderId = #{orderId} order by createTime desc limit 1")
    TripOrderRefund getRefundOrderByOrderId(String orderId);

    @Select("select * from trip_order_refund where id = #{refundId}")
    TripOrderRefund getRefundOrderById(Integer refundId);
}
