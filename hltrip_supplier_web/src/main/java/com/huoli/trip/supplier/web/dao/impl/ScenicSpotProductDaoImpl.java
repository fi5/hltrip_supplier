package com.huoli.trip.supplier.web.dao.impl;

import com.huoli.trip.common.entity.mpo.scenicSpotTicket.ScenicSpotProductMPO;
import com.huoli.trip.common.util.ListUtils;
import com.huoli.trip.supplier.web.dao.ScenicSpotProductDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
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
public class ScenicSpotProductDaoImpl implements ScenicSpotProductDao {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public ScenicSpotProductMPO getBySupplierProductId(String supplierProductId, String channel){
        return mongoTemplate.findOne(new Query(Criteria.where("supplierProductId")
                .is(supplierProductId).and("channel").is(channel)), ScenicSpotProductMPO.class);
    }

    @Override
    public List<String> getSupplierProductIdByChannel(String channel){
        Query query = new Query(Criteria.where("channel").is(channel));
        query.fields().include("supplierProductId");
        List<ScenicSpotProductMPO> scenicSpotProductMPOs = mongoTemplate.find(query, ScenicSpotProductMPO.class);
        if(ListUtils.isNotEmpty(scenicSpotProductMPOs)){
            return scenicSpotProductMPOs.stream().map(ScenicSpotProductMPO::getSupplierProductId).collect(Collectors.toList());
        }
        return null;
    }

    @Override
    public void updateStatusById(String id, Integer status){
        mongoTemplate.updateFirst(new Query(Criteria.where("id").is(id)),
                Update.update("status", status), ScenicSpotProductMPO.class);
    }
}
