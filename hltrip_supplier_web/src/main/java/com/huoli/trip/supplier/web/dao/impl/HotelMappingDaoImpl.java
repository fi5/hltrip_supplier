package com.huoli.trip.supplier.web.dao.impl;

import com.huoli.trip.common.entity.mpo.hotelScenicSpot.HotelMappingMPO;
import com.huoli.trip.common.entity.mpo.scenicSpotTicket.ScenicSpotMappingMPO;
import com.huoli.trip.common.util.ListUtils;
import com.huoli.trip.supplier.web.dao.HotelMappingDao;
import com.huoli.trip.supplier.web.dao.ScenicSpotMappingDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 描述：<br/>
 * 版权：Copyright (c) 2011-2020<br>
 * 公司：活力天汇<br>
 * 作者：冯志强<br>
 * 版本：1.0<br>
 * 创建日期：2021/3/23<br>
 */
@Repository
public class HotelMappingDaoImpl implements HotelMappingDao {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public HotelMappingMPO getHotelByChannelHotelIdAndChannel(String channelHotelId, String channel){
        return mongoTemplate.findOne(new Query(Criteria.where("channelHotelId").is(channelHotelId)
                .and("channel").is(channel)), HotelMappingMPO.class);
    }

    @Override
    public HotelMappingMPO addHotelMapping(HotelMappingMPO hotelMappingMPO){
        return mongoTemplate.insert(hotelMappingMPO);
    }

    @Override
    public List<String> getScenicSpotByChannel(String channel){
        List<HotelMappingMPO> hotelMappingMPOs = mongoTemplate.find(new Query(Criteria.where("channel").is(channel)), HotelMappingMPO.class);
        if(ListUtils.isNotEmpty(hotelMappingMPOs)){
            return hotelMappingMPOs.stream().map(HotelMappingMPO::getChannelHotelId).collect(Collectors.toList());
        }
        return null;
    }

}
