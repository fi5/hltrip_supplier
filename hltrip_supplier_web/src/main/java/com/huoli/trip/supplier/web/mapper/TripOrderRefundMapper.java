package com.huoli.trip.supplier.web.mapper;

import com.huoli.trip.common.entity.TripOrder;
import com.huoli.trip.common.entity.TripOrderRefund;
import com.huoli.trip.common.entity.TripOrderVoucher;
import com.huoli.trip.common.entity.TripRefundNotify;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Mapper
public interface TripOrderRefundMapper {

    @Select("select * from trip_order_refund where orderId = #{orderId} and status=0 order by createTime desc limit 1")
    TripOrderRefund getRefundingOrderByOrderId(String orderId);

    @Select("select * from trip_order_refund where id = #{refundId}")
    TripOrderRefund getRefundOrderById(Integer refundId);

    @Select("select * from trip_order_refund_notify where orderId = #{orderId} and refundId= #{refundId} limit 1")
    TripRefundNotify getRefundNotify(String orderId,Integer refundId);

    @Select("select * from trip_order_refund_notify where status=0 order by createTime desc limit 20")
    List<TripRefundNotify> getPendingNotifys();

    @Insert("insert into trip_order_refund_notify(orderId, status, channel, refundId,createTime) values" +
            "( #{orderId}, #{status}, #{channel}, #{refundId}, NOW())")
    void saveTripRefundNotify(TripRefundNotify notify);

    @Update("update trip_order set status = #{status}, refundTime = #{refundTime},refundStatus = #{refundStatus} " +
            ",refundMoney = #{refundMoney} ,billInfo = #{billInfo} where id = #{id}")
    void updateRefundNotify(TripRefundNotify notify);
}