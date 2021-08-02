package com.huoli.trip.supplier.web.dao.impl;

import com.huoli.trip.common.constant.MongoConst;
import com.huoli.trip.common.entity.mpo.ProductListMPO;
import com.huoli.trip.common.entity.mpo.groupTour.GroupTourProductMPO;
import com.huoli.trip.common.entity.mpo.scenicSpotTicket.ScenicSpotProductMPO;
import com.huoli.trip.common.util.ListUtils;
import com.huoli.trip.supplier.web.dao.GroupTourProductDao;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

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
    public void updateProduct(GroupTourProductMPO groupTourProductMPO) {
        Query query = new Query();
        query.addCriteria(Criteria.where("supplierProductId").is(groupTourProductMPO.getSupplierProductId()));
        GroupTourProductMPO one = mongoTemplate.findOne(query, GroupTourProductMPO.class);
        if (one != null) {
            groupTourProductMPO.setId(one.getId());
        }
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
    public void updateStatus(String supplierProductId, String channel, int status){
        mongoTemplate.updateFirst(new Query(Criteria.where("supplierProductId")
                .is(supplierProductId).and("channel").is(channel)), Update.update("status", status).set("updateTime", new Date()), GroupTourProductMPO.class);
    }

    @Override
    public void updateStatusById(String id, int status){
        mongoTemplate.updateFirst(new Query(Criteria.where("_id")
                .is(id)), Update.update("status", status).set("updateTime", new Date()), GroupTourProductMPO.class);
    }

    @Override
    public List<String> getSupplierProductIdByChannel(String channel){
        Query query = new Query(Criteria.where("channel").is(channel));
        query.fields().include("supplierProductId");
        List<GroupTourProductMPO> groupTourProductMPOs = mongoTemplate.find(query, GroupTourProductMPO.class);
        if(ListUtils.isNotEmpty(groupTourProductMPOs)){
            return groupTourProductMPOs.stream().map(GroupTourProductMPO::getSupplierProductId).collect(Collectors.toList());
        }
        return null;
    }
}
