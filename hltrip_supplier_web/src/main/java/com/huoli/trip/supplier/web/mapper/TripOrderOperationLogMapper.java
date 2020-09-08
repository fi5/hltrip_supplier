package com.huoli.trip.supplier.web.mapper;

import com.huoli.trip.common.entity.TripOrderOperationLog;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

@Repository
@Mapper
public interface TripOrderOperationLogMapper {
    @Insert("INSERT INTO  tirp_order_operation_log (orderId,oldStatus,newStatus,operator,updateTime,explain) VALUE(#{orderId},#{oldStatus},#{newStatus},#{operator},#{updateTime, jdbcType=TIMESTAMP},#{explain})")
    TripOrderOperationLog  insertOperationLog(TripOrderOperationLog tripOrderOperationLog);
}
