package com.huoli.trip.supplier.web.dao.impl;

import com.huoli.trip.common.constant.Constants;
import com.huoli.trip.common.entity.PriceInfoPO;
import com.huoli.trip.common.entity.ProductItemPO;
import com.huoli.trip.common.entity.ProductPO;
import com.huoli.trip.common.util.ListUtils;
import com.huoli.trip.supplier.web.dao.ProductItemDao;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
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
 * 创建日期：2020/6/28<br>
 */
@Repository
public class ProductItemDaoImpl implements ProductItemDao {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public void updateByCode(ProductItemPO productItemPO){
        Query query = new Query();
        query.addCriteria(Criteria.where("code").is(productItemPO.getCode()));
        Document document = new Document();
        mongoTemplate.getConverter().write(productItemPO, document);
        Update update = Update.fromDocument(document);
        mongoTemplate.upsert(query, update, Constants.COLLECTION_NAME_TRIP_PRODUCT_ITEM);
    }

    @Override
    public List<ProductItemPO> selectByCityAndType(String city, Integer type, int pageSize){
        Query query = new Query(Criteria.where("city").is(city).and("itemType").is(type)).limit(pageSize);
        List<ProductItemPO> productItems = mongoTemplate.find(query, ProductItemPO.class);
        return productItems;
    }

    @Override
    public ProductItemPO selectByCode(String code){
        Query query = new Query(Criteria.where("code").is(code));
        ProductItemPO productItem = mongoTemplate.findOne(query, ProductItemPO.class);
        return productItem;
    }

    @Override
    public void updateItemProductByCode(String code, ProductPO productPO){
        Query query = new Query();
        query.addCriteria(Criteria.where("code").is(code));
        mongoTemplate.updateFirst(query, Update.update("product", productPO), Constants.COLLECTION_NAME_TRIP_PRODUCT_ITEM);
    }

    @Override
    public List<ProductItemPO> selectCodes(){
        Query query = new Query();
        query.fields().include("code").exclude("_id");
        List<ProductItemPO> productItems = mongoTemplate.find(query, ProductItemPO.class);
        return productItems;
    }

    @Override
    public List<String> selectCodesBySupplierId(String supplierId){
        Query query = new Query(Criteria.where("supplierId").is(supplierId));
        query.fields().include("code");
        List<ProductItemPO> productItems = mongoTemplate.find(query, ProductItemPO.class);
        if(ListUtils.isNotEmpty(productItems)){
            return productItems.stream().map(ProductItemPO::getCode).collect(Collectors.toList());
        }
        return null;
    }

    @Override
    public void updateItemCoordinateByCode(String code, Double[] itemCoordinate){
        Query query = new Query();
        query.addCriteria(Criteria.where("code").is(code));
        mongoTemplate.updateFirst(query, Update.update("itemCoordinate", itemCoordinate), Constants.COLLECTION_NAME_TRIP_PRODUCT_ITEM);
    }

    @Override
    public List<String> selectSupplierItemIdsBySupplierIdAndType(String supplierId, Integer itemType){
        Query query = new Query(Criteria.where("supplierId").is(supplierId).and("itemType").is(itemType));
        query.fields().include("supplierItemId").exclude("_id");
        List<ProductItemPO> productItemPOs = mongoTemplate.find(query, ProductItemPO.class);
        if(ListUtils.isNotEmpty(productItemPOs)){
            return productItemPOs.stream().map(ProductItemPO::getSupplierItemId).collect(Collectors.toList());
        }
        return null;
    }

    @Override
    public List<ProductItemPO> selectAll(){
        return mongoTemplate.find(new Query(Criteria.where("itemType").is(2)), ProductItemPO.class);
    }

}
