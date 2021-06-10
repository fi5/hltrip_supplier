package com.huoli.trip.supplier.web.dao.impl;

import com.huoli.trip.common.constant.Constants;
import com.huoli.trip.common.entity.ProductItemPO;
import com.huoli.trip.common.entity.ProductPO;
import com.huoli.trip.common.entity.mpo.scenicSpotTicket.ScenicSpotProductMPO;
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
import java.util.stream.Collectors;
import java.util.Map;
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
    public void updateSupplierStatusByCode(String code, int supplierStatus){
        mongoTemplate.updateMulti(new Query().addCriteria(Criteria.where("supplierId").in(code)),
                Update.update("supplierStatus", supplierStatus), Constants.COLLECTION_NAME_TRIP_PRODUCT);
    }

    @Override
    public void updateAppFromByCode(String code, String appFrom){
        Update update = new Update();
        update.pull("appFrom", appFrom);
        mongoTemplate.updateMulti(new Query().addCriteria(Criteria.where("supplierId").in(code)),
                update, Constants.COLLECTION_NAME_TRIP_PRODUCT);
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
    public List<ProductPO> getBySupplierProductIdAndSupplierId(String supplierProductId, String supplierId){
        Query query = new Query(Criteria.where("supplierProductId").is(supplierProductId).and("supplierId").is(supplierId));
        return mongoTemplate.find(query, ProductPO.class);
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
                .and("status").is(Constants.PRODUCT_STATUS_VALID)
                .and("supplierStatus").is(Constants.SUPPLIER_STATUS_OPEN)
                .and("auditStatus").is(Constants.VERIFY_STATUS_PASSING)
                .and("remove").is(0)
                .and("priceCalendar.priceInfos.stock").gt(0)
                .and("priceCalendar.priceInfos.saleDate").gte(MongoDateUtils.handleTimezoneInput(DateTimeUtil.trancateToDate(new Date())))
                .and("priceCalendar.priceInfos.salePrice").gt(0);
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
                Fields.field("supplierStatus"),
                Fields.field("auditStatus"),
                Fields.field("productType"),
                Fields.field("images"),
                Fields.field("price"),
                Fields.field("salePrice"),
                Fields.field("city"),
                Fields.field("count"),
                Fields.field("priceCalendar"),
                Fields.field("validTime"),
                Fields.field("invalidTime"),
                Fields.field("appFrom"));
    }

    @Override
    public List<ProductPO> getProductsByStatus(int status){
        Query query = new Query(Criteria.where("status").is(status));
        return mongoTemplate.find(query, ProductPO.class);
    }

    @Override
    public ProductPO getByCode(String code){
        Query query = new Query(Criteria.where("code").is(code));
        return mongoTemplate.findOne(query, ProductPO.class);
    }

    @Override
    public List<ProductPO> getByRegexCode(String code){
        Query query = new Query(Criteria.where("code").regex(code));
        return mongoTemplate.find(query, ProductPO.class);
    }

    @Override
    public List<ProductPO> getSupplierProductIds(String supplierId, Integer productType){
        Query query = new Query(Criteria.where("supplierId").is(supplierId).and("productType").is(productType));
        query.fields().include("supplierProductId").exclude("_id");
        return mongoTemplate.find(query, ProductPO.class);
    }

    @Override
    public void updateVerifyStatusByCode(String code, int verifyStatus){
        mongoTemplate.updateFirst(new Query().addCriteria(Criteria.where("code").is(code)),
                Update.update("auditStatus", verifyStatus), Constants.COLLECTION_NAME_TRIP_PRODUCT);
    }

    @Override
    public List<String> selectSupplierProductIdsBySupplierIdAndType(String supplierId, Integer productType){
        Query query = new Query(Criteria.where("supplierId").is(supplierId).and("productType").is(productType));
        query.fields().include("supplierProductId").exclude("_id");
        List<ProductPO> productPOs = mongoTemplate.find(query, ProductPO.class);
        if(ListUtils.isNotEmpty(productPOs)){
            return productPOs.stream().map(ProductPO::getSupplierProductId).collect(Collectors.toList());
        }
        return null;
    }

    @Override
    public List<ProductPO> getBySupplierId(String supplierId){
        Query query = new Query(Criteria.where("supplierId").is(supplierId));
        return mongoTemplate.find(query, ProductPO.class);
    }

    @Override
    public List<ProductPO> getByCond(String channel, Map<String, String> cond){
        Criteria criteria = Criteria.where("channel").is(channel);
        for (String s : cond.keySet()) {
            criteria.and(s).is(cond.get(s));
        }
        return mongoTemplate.find(new Query(criteria), ProductPO.class);
    }

    @Override
    public List<String> selectSupplierProductIdsBySupplierIdAndType(String supplierId, Integer productType){
        Query query = new Query(Criteria.where("supplierId").is(supplierId).and("productType").is(productType));
        query.fields().include("supplierProductId").exclude("_id");
        List<ProductPO> productPOs = mongoTemplate.find(query, ProductPO.class);
        if(ListUtils.isNotEmpty(productPOs)){
            return productPOs.stream().map(ProductPO::getSupplierProductId).collect(Collectors.toList());
        }
        return null;
    }

}
