package com.huoli.trip.supplier.web.dao.impl;

import com.huoli.trip.common.constant.Constants;
import com.huoli.trip.common.entity.mpo.scenicSpotTicket.ScenicSpotMappingMPO;
import com.huoli.trip.common.util.ListUtils;
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
public class ScenicSpotMappingDaoImpl implements ScenicSpotMappingDao {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public ScenicSpotMappingMPO getScenicSpotByChannelScenicSpotIdAndChannel(String channelScenicSpotId, String channel){
        return mongoTemplate.findOne(new Query(Criteria.where("channelScenicSpotId").is(channelScenicSpotId)
                .and("channel").is(channel)), ScenicSpotMappingMPO.class);
    }

    @Override
    public ScenicSpotMappingMPO addScenicSpotMapping(ScenicSpotMappingMPO scenicSpotMappingMPO){
        return mongoTemplate.insert(scenicSpotMappingMPO);
    }

    @Override
    public List<String> getScenicSpotByChannel(String channel){
        List<ScenicSpotMappingMPO> scenicSpotMappingMPOs = mongoTemplate.find(new Query(Criteria.where("channel").is(channel)), ScenicSpotMappingMPO.class);
        if(ListUtils.isNotEmpty(scenicSpotMappingMPOs)){
            return scenicSpotMappingMPOs.stream().map(ScenicSpotMappingMPO::getChannelScenicSpotId).collect(Collectors.toList());
        }
        return null;
    }

}
