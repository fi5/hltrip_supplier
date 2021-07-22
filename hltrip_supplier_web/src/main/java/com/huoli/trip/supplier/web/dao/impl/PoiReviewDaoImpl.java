package com.huoli.trip.supplier.web.dao.impl;

import com.huoli.trip.common.entity.mpo.PoiReviewMPO;
import com.huoli.trip.common.util.ListUtils;
import com.huoli.trip.supplier.web.dao.PoiReviewDao;
import org.apache.commons.lang3.StringUtils;
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
public class PoiReviewDaoImpl implements PoiReviewDao {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public PoiReviewMPO addPoiReview(PoiReviewMPO poiReviewMPO){
        return mongoTemplate.insert(poiReviewMPO);
    }

    @Override
    public List<PoiReviewMPO> getPoiReviewByPage(int page, int size){
        return mongoTemplate.find(Query.query(new Criteria()).skip((page - 1) * size).limit(size), PoiReviewMPO.class);
    }

    @Override
    public void updateCity(PoiReviewMPO poiReviewMPO){
        mongoTemplate.updateFirst(Query.query(Criteria.where("_id").is(poiReviewMPO.getId())),
                Update.update("cityCode", poiReviewMPO.getCityCode()), PoiReviewMPO.class);
    }
}
