package com.huoli.trip.supplier.web.dao.impl;

import com.huoli.trip.common.constant.MongoConst;
import com.huoli.trip.common.entity.mpo.ProductUpdateNoticeMPO;
import com.huoli.trip.common.entity.mpo.scenicSpotTicket.ScenicSpotMPO;
import com.huoli.trip.supplier.web.dao.ProductUpdateNoticeDao;
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
 * 创建日期：2021/4/16<br>
 */
@Repository
public class ProductUpdateNoticeDaoImpl implements ProductUpdateNoticeDao {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public ProductUpdateNoticeMPO getUnreadNotice(int type, String userId, String productId){
        return mongoTemplate.findOne(new Query(Criteria.where("noticeStatus").is(0).and("type").is(type).and("userId").is(userId).and("productId").is(productId)),
                ProductUpdateNoticeMPO.class);
    }

    @Override
    public void saveProductUpdateNotice(ProductUpdateNoticeMPO productUpdateNoticeMPO){
        Query query = new Query();
        query.addCriteria(Criteria.where("_id").is(productUpdateNoticeMPO.getId()));
        Document document = new Document();
        mongoTemplate.getConverter().write(productUpdateNoticeMPO, document);
        Update update = Update.fromDocument(document);
        mongoTemplate.upsert(query, update, MongoConst.COLLECTION_NAME_PRODUCT_UPDATE_NOTICE);
    }
}
