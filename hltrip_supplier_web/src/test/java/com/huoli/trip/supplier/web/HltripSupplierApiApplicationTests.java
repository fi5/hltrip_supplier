package com.huoli.trip.supplier.web;

import com.huoli.trip.common.entity.ProductItemPO;
import com.huoli.trip.common.util.CoordinateUtil;
import com.huoli.trip.common.vo.ProductItem;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.List;

@SpringBootTest
@Slf4j
class HltripSupplierApiApplicationTests {

    @Autowired
    private MongoTemplate mongoTemplate;

//    @Test
    void contextLoads() {
    }

//    @Test
    public void test0(){
       List<ProductItemPO> productItemPOList =  mongoTemplate.find(Query.query(Criteria.where("itemCoordinate").ne("").ne(null)), ProductItemPO.class);
       productItemPOList.forEach(productItemPO -> {
           if(productItemPO.getItemCoordinate() != null && productItemPO.getItemCoordinate().length == 2){
               try {
                   double[] d = CoordinateUtil.bd09_To_Gcj02(productItemPO.getItemCoordinate()[1], productItemPO.getItemCoordinate()[0]);
                   mongoTemplate.updateFirst(Query.query(Criteria.where("code").is(productItemPO.getCode())),
                           Update.update("itemCoordinate", new Double[]{d[1],d[0]}), productItemPO.getClass());
               } catch (Exception e) {
                   log.error("异常", e);
               }
           }
       });
    }

}
