package com.huoli.trip.supplier.web.config;

import com.huoli.trip.common.config.ConvertToBigDecimal;
import com.huoli.trip.common.config.ConvertToDouble;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;

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
public class MongoConvertConfig {

    @Bean
    public MongoCustomConversions mongoCustomConversions() {
        List<Converter<?, ?>> converterList = new ArrayList<>();
        converterList.add(new ConvertToBigDecimal());
        converterList.add(new ConvertToDouble());
        return new MongoCustomConversions(converterList);
    }

}
