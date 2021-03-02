package com.huoli.trip.supplier.web.config;

import com.google.common.collect.Sets;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisNode;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisShardInfo;

import java.util.Set;

/**
 * 描述: redis配置类<br>
 * 版权：Copyright (c) 2011-2020<br>
 * 公司：活力天汇<br>
 * 作者：冯志强<br>
 * 版本：1.0<br>
 * 创建日期：2018/12/6<br>
 */
@Configuration
public class RedisConfig {

    /**
     * redis集群地址
     */
    @Value("${redis.cluster}")
    private String cluster;
    /**
     * redis服务端地址
     */
    @Value("${redis.host}")
    private String host;
    /**
     * 是否集群
     */
    @Value("${redis.iscluster}")
    private String iscluster;

    /**
     * 连接池配置
     * @return
     */
    private JedisPoolConfig poolConfig() {
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(1024);
        poolConfig.setMaxIdle(200);
        poolConfig.setMinIdle(1);
        poolConfig.setTestOnBorrow(true);
        poolConfig.setTestOnReturn(true);
        poolConfig.setTestWhileIdle(true);
        return poolConfig;
    }

    /**
     * 集群配置
     * @return
     */
    private RedisClusterConfiguration clusterConfiguration() {
        RedisClusterConfiguration clusterConfiguration = new RedisClusterConfiguration();
        clusterConfiguration.setMaxRedirects(3);
        String[] nodeArr = cluster.split(",");
        Set<RedisNode> set = Sets.newHashSet();
        for (String node : nodeArr) {
            String[] nn = node.split(":");
            RedisNode redisNode = new RedisNode(nn[0], Integer.valueOf(nn[1]));
            set.add(redisNode);
        }
        clusterConfiguration.setClusterNodes(set);
        return clusterConfiguration;
    }

    /**
     * 初始化客户端
     * @return
     */
    private JedisShardInfo jedisShardInfo() {
        return new JedisShardInfo(host.split(":")[0], Integer.parseInt(host.split(":")[1]));
    }

    /**
     * 工厂配置
     * @return
     */
    @Bean
    public JedisConnectionFactory jedisConnectionFactory() {
        Integer isCluster = Integer.parseInt(iscluster);
        JedisConnectionFactory connectionFactory;
        if (isCluster == 1) {
            connectionFactory = new JedisConnectionFactory(clusterConfiguration());
        } else {
            connectionFactory = new JedisConnectionFactory(jedisShardInfo());
        }
        connectionFactory.setPoolConfig(poolConfig());
        connectionFactory.setTimeout(5000);
        return connectionFactory;
    }

    /**
     * reids服务类
     * @param jedisConnectionFactory
     * @return
     */
    @Bean(name = "stringJedisTemplate")
    public StringRedisTemplate stringRedisTemplate(@Qualifier("jedisConnectionFactory") JedisConnectionFactory jedisConnectionFactory) {
        return new StringRedisTemplate(jedisConnectionFactory);
    }

    /**
     * redis服务类
     * @param jedisConnectionFactory
     * @return
     */
    @SuppressWarnings("unchecked")
    @Bean(name = "jedisTemplate")
    public RedisTemplate redisTemplate(@Qualifier("jedisConnectionFactory") JedisConnectionFactory jedisConnectionFactory) {
        RedisTemplate redisTemplate = new RedisTemplate();
        redisTemplate.setConnectionFactory(jedisConnectionFactory);
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new StringRedisSerializer());
        redisTemplate.setHashValueSerializer(new StringRedisSerializer());
        redisTemplate.setEnableTransactionSupport(false);//cluster不支持事务
        return redisTemplate;
    }
}

