package com.huoli.trip.supplier.web.dao.impl;

import com.huoli.trip.common.entity.mpo.SubscribeProductMPO;
import com.huoli.trip.common.util.ListUtils;
import com.huoli.trip.supplier.web.dao.SubscribeProductDao;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
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
public class SubscribeProductDaoImpl implements SubscribeProductDao {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public List<SubscribeProductMPO> getByCond(String category, String city,
                                               String poiId, String channel, String element,
                                               List<String> depCity, List<String> arrCity, String productId){
        Criteria criteria = Criteria.where("category").is(category);
        if(StringUtils.isNotBlank(city)){
            criteria.and("cityCode").is(city);
        }
        if(StringUtils.isNotBlank(poiId)){
            criteria.and("poiId").is(poiId);
        }
        if(StringUtils.isNotBlank(channel)){
            criteria.and("channelCodes").is(channel);
        }
        if(StringUtils.isNotBlank(element)){
            criteria.and("elements").is(element);
        }
        if(ListUtils.isNotEmpty(depCity)){
            criteria.and("cityCode").in(depCity);
        }
        if(ListUtils.isNotEmpty(arrCity)){
            criteria.and("arrCityCode").in(arrCity);
        }
        if(StringUtils.isNotBlank(productId)){
            criteria.and("productId");
        }
        Query query = new Query(criteria);
        query.fields().include("userId");
        return mongoTemplate.find(query, SubscribeProductMPO.class);
    }

    @Override
    public List<SubscribeProductMPO> getByCategory(String category){
        Query query = new Query(Criteria.where("category").is(category));
        return mongoTemplate.find(query, SubscribeProductMPO.class);
    }
}
