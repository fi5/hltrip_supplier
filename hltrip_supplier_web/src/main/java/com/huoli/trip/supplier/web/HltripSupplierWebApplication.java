package com.huoli.trip.supplier.web;

import com.alibaba.dubbo.config.spring.context.annotation.DubboComponentScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.cloud.netflix.hystrix.EnableHystrix;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ImportResource;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@SpringBootApplication(exclude = {RedisAutoConfiguration.class, RedisRepositoriesAutoConfiguration.class,
        DataSourceAutoConfiguration.class, DataSourceTransactionManagerAutoConfiguration.class})
@ImportResource({"classpath:disconf-config.xml"})
@ComponentScan({"com.huoli.trip"})
@DubboComponentScan(value = "com.huoli.trip.supplier.web.service")
@EnableFeignClients(basePackages = "com.huoli.trip.supplier.feign.client.yaochufa.client")
@EnableHystrix
@EnableSwagger2
public class HltripSupplierWebApplication {

    public static void main(String[] args) {
        SpringApplication.run(HltripSupplierWebApplication.class, args);
    }

}
