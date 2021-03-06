package com.huoli.trip.supplier.web.dao.impl;

import com.huoli.trip.common.constant.MongoConst;
import com.huoli.trip.common.entity.mpo.groupTour.GroupTourPrice;
import com.huoli.trip.common.entity.mpo.groupTour.GroupTourProductSetMealMPO;
import com.huoli.trip.supplier.web.dao.GroupTourProductSetMealDao;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 描述：<br/>
 * 版权：Copyright (c) 2011-2020<br>
 * 公司：活力天汇<br>
 * 作者：冯志强<br>
 * 版本：1.0<br>
 * 创建日期：2021/3/31<br>
 */
@Repository
public class GroupTourProductSetMealDaoImpl implements GroupTourProductSetMealDao {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public void addSetMeals(GroupTourProductSetMealMPO setMealMPO){
        mongoTemplate.insert(setMealMPO);
    }

    @Override
    public void saveSetMeals(GroupTourProductSetMealMPO setMealMPO){
        Query query = new Query();
        query.addCriteria(Criteria.where("_id").is(setMealMPO.getId()));
        Document document = new Document();
        mongoTemplate.getConverter().write(setMealMPO, document);
        Update update = Update.fromDocument(document);
        mongoTemplate.upsert(query, update, MongoConst.COLLECTION_NAME_GROUPTOUR_PRODUCT_SET_MEAL);
    }

    @Override
    public void updateSetMeals(GroupTourProductSetMealMPO setMealMPO) {
        Query query = new Query();
        query.addCriteria(Criteria.where("groupTourProductId").is(setMealMPO.getGroupTourProductId()));
        GroupTourProductSetMealMPO one = mongoTemplate.findOne(query, GroupTourProductSetMealMPO.class);
        if (one != null) {
            setMealMPO.setId(one.getId());
            setMealMPO.setCreateTime(one.getCreateTime());
        }
        Document document = new Document();
        mongoTemplate.getConverter().write(setMealMPO, document);
        Update update = Update.fromDocument(document);
        mongoTemplate.upsert(query, update, MongoConst.COLLECTION_NAME_GROUPTOUR_PRODUCT_SET_MEAL);
    }

    @Override
    public GroupTourProductSetMealMPO getSetMeal(String groupTourProductId, String depCode){
        return mongoTemplate.findOne(new Query(Criteria.where("groupTourProductId").is(groupTourProductId)
                .and("depCode").is(depCode)), GroupTourProductSetMealMPO.class);
    }

    @Override
    public List<GroupTourProductSetMealMPO> getSetMealByProductId(String groupTourProductId){
        return mongoTemplate.find(new Query(Criteria.where("groupTourProductId").is(groupTourProductId)), GroupTourProductSetMealMPO.class);
    }

    @Override
    public GroupTourProductSetMealMPO getSetMealByPackageId(String packageId) {
        Query query = new Query();
        Criteria criteria = new Criteria();
        criteria.and("_id").is(packageId);
        query.addCriteria(criteria);
        return mongoTemplate.findOne(query, GroupTourProductSetMealMPO.class);
    }

    @Override
    public void updatePriceStock(GroupTourProductSetMealMPO groupTourProductSetMealMPO, GroupTourPrice groupTourPrice) {
        mongoTemplate.updateMulti(Query.query(Criteria.where("_id").is(groupTourProductSetMealMPO.getId())
                .andOperator(Criteria.where("groupTourPrices.date").is(groupTourPrice.getDate()))),
                Update.update("groupTourPrices.$.adtStock", groupTourPrice.getAdtStock())
                .set("groupTourPrices.$.chdStock", groupTourPrice.getChdStock()), GroupTourProductSetMealMPO.class);
    }
}
