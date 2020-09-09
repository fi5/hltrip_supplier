package com.huoli.trip.supplier.web.mapper;

import com.huoli.trip.common.entity.TripOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

@Repository
@Mapper
public interface TripOrderMapper {
    @Select("SELECT status FROM trip_order WHERE orderId = #{orderId}")
    TripOrder getOrderStatusByOrderId(String orderId);
}
