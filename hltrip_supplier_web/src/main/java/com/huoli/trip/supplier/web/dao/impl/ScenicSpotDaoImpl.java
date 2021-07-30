package com.huoli.trip.supplier.web.dao.impl;

import com.huoli.trip.common.constant.MongoConst;
import com.huoli.trip.common.entity.mpo.scenicSpotTicket.ScenicSpotMPO;
import com.huoli.trip.common.entity.mpo.scenicSpotTicket.ScenicSpotRuleMPO;
import com.huoli.trip.supplier.web.dao.ScenicSpotDao;
import org.apache.commons.lang3.StringUtils;
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
 * 创建日期：2021/3/23<br>
 */
@Repository
public class ScenicSpotDaoImpl implements ScenicSpotDao {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public ScenicSpotMPO getScenicSpotByNameAndAddress(String name, String cityCode){
        Criteria criteria = Criteria.where("name").is(name);
        if(StringUtils.isNotBlank(cityCode)){
            criteria.and("cityCode").is(cityCode);
        }
        return mongoTemplate.findOne(new Query(criteria), ScenicSpotMPO.class);
    }

    @Override
    public ScenicSpotMPO addScenicSpot(ScenicSpotMPO scenicSpotMPO){
        return mongoTemplate.insert(scenicSpotMPO);
    }

    @Override
    public ScenicSpotMPO getScenicSpotById(String id){
        return mongoTemplate.findOne(new Query(Criteria.where("_id").is(id)), ScenicSpotMPO.class);
    }

    @Override
    public void saveScenicSpot(ScenicSpotMPO scenicSpotMPO){
        Query query = new Query();
        query.addCriteria(Criteria.where("_id").is(scenicSpotMPO.getId()));
        Document document = new Document();
        mongoTemplate.getConverter().write(scenicSpotMPO, document);
        Update update = Update.fromDocument(document);
        mongoTemplate.upsert(query, update, MongoConst.COLLECTION_NAME_SCENICSPOT);
    }

    @Override
    public List<ScenicSpotMPO> getdetailDesc(){
        Query query = new Query();
        query.addCriteria(Criteria.where("detailDesc").ne("").ne(null));
        return mongoTemplate.find(query, ScenicSpotMPO.class);
    }

    @Override
    public List<ScenicSpotMPO> getNetImages(){
        Query query = new Query();
        query.addCriteria(Criteria.where("images").regex(".*/(?!intticket).*\\.(?!aliyuncs).*"));
        return mongoTemplate.find(query, ScenicSpotMPO.class);
    }

    @Override
    public List<ScenicSpotMPO> getNetImagesByIds(List<String> ids){
        Query query = new Query();
        query.addCriteria(Criteria.where("images").regex(".*/(?!intticket).*\\.(?!aliyuncs).*"));
        query.addCriteria(Criteria.where("_id").in(ids));
        return mongoTemplate.find(query, ScenicSpotMPO.class);
    }

    @Override
    public void updateImagesById(List<String> images,String id){
        Query query = Query.query(Criteria.where("_id").is(id));
        Update update = new Update();
        update.set("images",images);
        mongoTemplate.updateFirst(query, update, ScenicSpotMPO.class);
    }

    @Override
    public void updateDeatailDescById(String detailDesc,String id){
        Query query = Query.query(Criteria.where("_id").is(id));
        Update update = new Update();
        update.set("detailDesc",detailDesc);
        mongoTemplate.updateFirst(query, update, ScenicSpotMPO.class);
    }
}
