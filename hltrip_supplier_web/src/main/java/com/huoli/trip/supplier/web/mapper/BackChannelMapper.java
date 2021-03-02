package com.huoli.trip.supplier.web.mapper;

import com.huoli.trip.common.entity.BackChannelEntry;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author lunatic
 * @Title: BackChannel
 * @Package
 * @Description: 后台渠道信息
 * @date 2020/12/110:12
 */

@Repository
@Mapper
public interface BackChannelMapper {
    @Select("select channel,payNoticePhone,refundNoticePhone,status,appSource from trip_back_channel where  channel= #{channelCode}")
    BackChannelEntry getChannelInfoByChannelCode(String channelCode);
}
