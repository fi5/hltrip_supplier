package com.huoli.trip.supplier.web.dao.impl;

import com.huoli.trip.common.constant.MongoConst;
import com.huoli.trip.common.entity.mpo.scenicSpotTicket.ScenicSpotProductMPO;
import com.huoli.trip.common.entity.mpo.scenicSpotTicket.ScenicSpotProductPriceMPO;
import com.huoli.trip.supplier.web.dao.ScenicSpotProductPriceDao;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.BulkOperations;
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
 * 创建日期：2021/3/24<br>
 */
@Repository
public class ScenicSpotProductPriceDaoImpl implements ScenicSpotProductPriceDao {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public ScenicSpotProductPriceMPO addScenicSpotProductPrice(ScenicSpotProductPriceMPO scenicSpotProductPriceMPO){
        return mongoTemplate.insert(scenicSpotProductPriceMPO);
    }

    @Override
    public void saveScenicSpotProductPrice(ScenicSpotProductPriceMPO scenicSpotProductPriceMPO){
        Document document = new Document();
        mongoTemplate.getConverter().write(scenicSpotProductPriceMPO, document);
        Update update = Update.fromDocument(document);
        mongoTemplate.upsert(Query.query(Criteria.where("_id").is(scenicSpotProductPriceMPO.getId())),
                update, MongoConst.COLLECTION_NAME_SCENICSPOT_PRODUCT_PRICE);
    }

    @Override
    public void saveScenicSpotProductPrice(List<ScenicSpotProductPriceMPO> scenicSpotProductPriceMPOs){
        BulkOperations bulk = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, ScenicSpotProductPriceMPO.class);
        for (ScenicSpotProductPriceMPO scenicSpotProductPriceMPO : scenicSpotProductPriceMPOs) {
            Document document = new Document();
            mongoTemplate.getConverter().write(scenicSpotProductPriceMPO, document);
            Update update = Update.fromDocument(document);
            bulk.upsert(Query.query(Criteria.where("_id").is(scenicSpotProductPriceMPO.getId())), update);
        }
        bulk.execute();
    }

    @Override
    public ScenicSpotProductPriceMPO getPriceByPackageId(String packageId) {
        Query query = new Query();
        Criteria criteria = new Criteria();
        criteria.and("_id").is(packageId);
        query.addCriteria(criteria);
        return mongoTemplate.findOne(query, ScenicSpotProductPriceMPO.class);
    }

    @Override
    public void updatePriceStock(ScenicSpotProductPriceMPO priceMPO) {
        mongoTemplate.updateMulti(Query.query(Criteria.where("_id").is(priceMPO.getId())),
                Update.update("stock", priceMPO.getStock()), ScenicSpotProductPriceMPO.class);
    }

    @Override
    public List<ScenicSpotProductPriceMPO> getByProductId(String productId){
        return mongoTemplate.find(new Query(Criteria.where("scenicSpotProductId").is(productId)), ScenicSpotProductPriceMPO.class);
    }

    @Override
    public ScenicSpotProductPriceMPO getExistPrice(String productId, String ruleId, String startDate){
        return mongoTemplate.findOne(new Query(Criteria.where("scenicSpotProductId").is(productId)
                .and("scenicSpotRuleId").is(ruleId).and("startDate").is(startDate)), ScenicSpotProductPriceMPO.class);
    }
}
