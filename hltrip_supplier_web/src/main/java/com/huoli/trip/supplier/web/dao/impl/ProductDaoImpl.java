package com.huoli.trip.supplier.web.dao.impl;

import com.huoli.trip.common.constant.Constants;
import com.huoli.trip.common.entity.ProductPO;
import com.huoli.trip.common.util.DateTimeUtil;
import com.huoli.trip.common.util.ListUtils;
import com.huoli.trip.common.util.MongoDateUtils;
import com.huoli.trip.supplier.web.dao.ProductDao;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

/**
 * 描述：<br/>
 * 版权：Copyright (c) 2011-2020<br>
 * 公司：活力天汇<br>
 * 作者：冯志强<br>
 * 版本：1.0<br>
 * 创建日期：2020/6/28<br>
 */
@Repository
public class ProductDaoImpl implements ProductDao {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public void updateByCode(ProductPO productPO){
        Query query = new Query();
        query.addCriteria(Criteria.where("code").is(productPO.getCode()));
        Document document = new Document();
        mongoTemplate.getConverter().write(productPO, document);
        Update update = Update.fromDocument(document);
        mongoTemplate.upsert(query, update, Constants.COLLECTION_NAME_TRIP_PRODUCT);
    }

    @Override
    public void updateStatusByCode(String code, int status){
        mongoTemplate.updateFirst(new Query().addCriteria(Criteria.where("code").is(code)),
                Update.update("status", status), Constants.COLLECTION_NAME_TRIP_PRODUCT);
    }

    @Override
    public List<ProductPO> getProductListByItemIds(List<String> itemIds){
        Query query = new Query(Criteria.where("mainItemCode").in(itemIds));
        return mongoTemplate.find(query, ProductPO.class);
    }

    @Override
    public ProductPO getBySupplierProductId(String supplierProductId){
        Query query = new Query(Criteria.where("supplierProductId").is(supplierProductId));
        return mongoTemplate.findOne(query, ProductPO.class);
    }

    @Override
    public List<ProductPO> getCodeBySupplierId(String supplierId){
        Query query = new Query(Criteria.where("supplierId").is(supplierId));
        query.fields().include("code").include("supplierProductId");
        return mongoTemplate.find(query, ProductPO.class);
    }

    @Override
    public ProductPO getProductListByItemId(String itemId){
        // 连价格日历表
        LookupOperation priceLookup = LookupOperation.newLookup().from(Constants.COLLECTION_NAME_TRIP_PRICE_CALENDAR)
                .localField("code")
                .foreignField("productCode")
                .as("priceCalendar");
        // 拆价格日历
        UnwindOperation unwindOperation = Aggregation.unwind("priceCalendar");
        UnwindOperation unwindOperation1 = Aggregation.unwind("priceCalendar.priceInfos");
        // 按价格正序
        SortOperation priceSort = Aggregation.sort(Sort.Direction.ASC, "priceCalendar.priceInfos.salePrice");
        // 查询条件
        Criteria criteria = Criteria.where("mainItemCode").is(itemId)
                .and("status").is(1)
                .and("remove").is(0)
                .and("priceCalendar.priceInfos.stock").gt(0)
                .and("priceCalendar.priceInfos.saleDate").gte(MongoDateUtils.handleTimezoneInput(DateTimeUtil.trancateToDate(new Date())))
                .and("priceCalendar.priceInfos.salePrice").ne(null);
        MatchOperation matchOperation = Aggregation.match(criteria);
        // 指定字段
        ProjectionOperation projectionOperation = Aggregation.project(getProductListFields()).andExclude("_id");
        // 分组后排序
        Aggregation aggregation = Aggregation.newAggregation(priceLookup,
                unwindOperation,
                unwindOperation1,
                matchOperation,
                priceSort,
                projectionOperation,
                Aggregation.limit(1));
        AggregationResults<ProductPO> output = mongoTemplate.aggregate(aggregation, Constants.COLLECTION_NAME_TRIP_PRODUCT, ProductPO.class);
        if(ListUtils.isNotEmpty(output.getMappedResults())){
            return output.getMappedResults().get(0);
        }
        return null;
    }

    private Fields getProductListFields(){
        return Fields.from(Fields.field("mainItemCode"),
                Fields.field("code"),
                Fields.field("name"),
                Fields.field("status"),
                Fields.field("productType"),
                Fields.field("images"),
                Fields.field("price"),
                Fields.field("salePrice"),
                Fields.field("city"),
                Fields.field("count"),
                Fields.field("priceCalendar"),
                Fields.field("validTime"),
                Fields.field("invalidTime"));
    }

    @Override
    public ProductPO getByCode(String code){
        Query query = new Query(Criteria.where("code").is(code));
        return mongoTemplate.findOne(query, ProductPO.class);
    }

    @Override
    public List<ProductPO> getSupplierProductIds(String supplierId){
        Query query = new Query(Criteria.where("supplierId").is(supplierId));
        query.fields().include("supplierProductId").exclude("_id");
        return mongoTemplate.find(query, ProductPO.class);
    }
}
