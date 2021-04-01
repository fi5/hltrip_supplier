package com.huoli.trip.supplier.web.dao.impl;

import com.huoli.trip.common.constant.MongoConst;
import com.huoli.trip.common.entity.mpo.scenicSpotTicket.ScenicSpotProductMPO;
import com.huoli.trip.common.entity.mpo.scenicSpotTicket.ScenicSpotRuleMPO;
import com.huoli.trip.supplier.web.dao.ScenicSpotRuleDao;
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
 * 创建日期：2021/3/24<br>
 */
@Repository
public class ScenicSpotRuleDaoImpl implements ScenicSpotRuleDao {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public ScenicSpotRuleMPO addScenicSpotRule(ScenicSpotRuleMPO scenicSpotRuleMPO){
        return mongoTemplate.insert(scenicSpotRuleMPO);
    }

    @Override
    public void saveScenicSpotRule(ScenicSpotRuleMPO scenicSpotRuleMPO){
        Query query = new Query();
        query.addCriteria(Criteria.where("_id").is(scenicSpotRuleMPO.getId()));
        Document document = new Document();
        mongoTemplate.getConverter().write(scenicSpotRuleMPO, document);
        Update update = Update.fromDocument(document);
        mongoTemplate.upsert(query, update, MongoConst.COLLECTION_NAME_SCENICSPOT_RULE);
    }
}
