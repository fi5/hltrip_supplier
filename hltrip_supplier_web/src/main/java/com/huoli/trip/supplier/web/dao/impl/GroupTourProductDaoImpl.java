package com.huoli.trip.supplier.web.dao.impl;

import com.huoli.trip.common.constant.MongoConst;
import com.huoli.trip.common.entity.mpo.groupTour.GroupTourProductMPO;
import com.huoli.trip.common.entity.mpo.scenicSpotTicket.ScenicSpotProductMPO;
import com.huoli.trip.supplier.web.dao.GroupTourProductDao;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

/**
 * 描述：<br/>
 * 版权：Copyright (c) 2011-2020<br>
 * 公司：活力天汇<br>
 * 作者：冯志强<br>
 * 版本：1.0<br>
 * 创建日期：2021/3/31<br>
 */
@Repository
public class GroupTourProductDaoImpl implements GroupTourProductDao {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public void saveProduct(GroupTourProductMPO groupTourProductMPO){
        Query query = new Query();
        query.addCriteria(Criteria.where("_id").is(groupTourProductMPO.getId()));
        Document document = new Document();
        mongoTemplate.getConverter().write(groupTourProductMPO, document);
        Update update = Update.fromDocument(document);
        mongoTemplate.upsert(query, update, MongoConst.COLLECTION_NAME_GROUPTOUR_PRODUCT);
    }

    @Override
    public void addProduct(GroupTourProductMPO groupTourProductMPO){
        mongoTemplate.insert(groupTourProductMPO);
    }

    @Override
    public GroupTourProductMPO getTourProduct(String supplierProductId, String channel){
        return mongoTemplate.findOne(new Query(Criteria.where("supplierProductId")
                .is(supplierProductId).and("channel").is(channel)), GroupTourProductMPO.class);
    }

    @Override
    public GroupTourProductMPO getTourProduct(String supplierProductId, String channel, String cityName){
        return mongoTemplate.findOne(new Query(Criteria.where("supplierProductId")
                .is(supplierProductId).and("channel").is(channel).and("depInfos.$cityName").is(cityName)), GroupTourProductMPO.class);
    }

    @Override
    public void updateStatus(String supplierProductId, String channel){
        mongoTemplate.updateFirst(new Query(Criteria.where("supplierProductId")
                .is(supplierProductId).and("channel").is(channel)), Update.update("status", 3), GroupTourProductMPO.class);
    }
}
