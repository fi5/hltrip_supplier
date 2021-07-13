package com.huoli.trip.supplier.web.config;

import com.huoli.trip.common.config.ConvertToBigDecimal;
import com.huoli.trip.common.config.ConvertToDouble;
import com.huoli.trip.common.constant.ConfigConstants;
import com.huoli.trip.common.util.ConfigGetter;
import com.mongodb.ReadPreference;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;
import org.springframework.data.mongodb.core.convert.DefaultDbRefResolver;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;

import java.util.ArrayList;
import java.util.List;

/**
 * 描述：<br/>
 * 版权：Copyright (c) 2011-2020<br>
 * 公司：活力天汇<br>
 * 作者：冯志强<br>
 * 版本：1.0<br>
 * 创建日期：2020/7/10<br>
 */
@Configuration
public class MongoConfig {

    @Bean
    public MappingMongoConverter mappingMongoConverter(){
        DefaultDbRefResolver dbRefResolver = new DefaultDbRefResolver(this.dbFactory());
        MappingMongoConverter converter = new MappingMongoConverter(dbRefResolver, this.mongoMappingContext());
        List<Object> list = new ArrayList<>();
        list.add(new ConvertToBigDecimal());//自定义的类型转换器
        list.add(new ConvertToDouble());//自定义的类型转换器
        converter.setCustomConversions(new MongoCustomConversions(list));
        return converter;
    }

    @Bean
    public MongoDatabaseFactory dbFactory(){
        return new SimpleMongoClientDatabaseFactory(ConfigGetter.getByFileItemString(ConfigConstants.CONFIG_FILE_NAME_COMMON, ConfigConstants.CONFIG_ITEM_MONGO_URI));
    }

    @Bean
    public MongoMappingContext mongoMappingContext() {
        MongoMappingContext mappingContext = new MongoMappingContext();
        mappingContext.setAutoIndexCreation(true);
        mappingContext.afterPropertiesSet();
        return mappingContext;
    }

    @Bean
    public MongoTemplate mongoTemplate(){
        MongoTemplate mongoTemplate = new MongoTemplate(this.dbFactory(), this.mappingMongoConverter());
        mongoTemplate.setReadPreference(ReadPreference.secondaryPreferred());
        return mongoTemplate;
    }
}
