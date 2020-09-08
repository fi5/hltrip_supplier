package com.huoli.trip.supplier.web.config;

import com.alibaba.druid.pool.DruidDataSource;
import com.huoli.trip.common.util.ConfigGetter;
import com.huoli.trip.supplier.self.common.ConstConfig;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.SqlSessionFactoryBean;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.ClassPathResource;

import javax.sql.DataSource;
import java.sql.SQLException;

@Configuration
@Slf4j
public class DruidConfiguration {

    /**
     * mybatis 配置路径
     */
    private static String MYBATIS_CONFIG = "mybatis-config.xml";
    @Bean(name = "huolitripDataSource")
    @Primary
    public DataSource tripDataSource() {
        DruidDataSource dataSource = createDruidDataSource(ConstConfig.DB_HUOLITRIP_FILE, "huolitrip");
        return dataSource;
    }


    private DruidDataSource createDruidDataSource(String fileName, String prefix) {
        DruidProperties druidProperties = createDruidProperties(fileName, prefix);
        DruidDataSource dataSource = new DruidDataSource();
        dataSource.setDriverClassName(druidProperties.getDriverClassName());
        dataSource.setUrl(druidProperties.getUrl());
        dataSource.setUsername(druidProperties.getUsername());
        dataSource.setPassword(druidProperties.getPassword());
        // configuration
        dataSource.setInitialSize(druidProperties.getInitialSize());
        dataSource.setMinIdle(druidProperties.getMinIdle());
        dataSource.setMaxActive(druidProperties.getMaxActive());
        dataSource.setMaxWait(druidProperties.getMaxWait());
        dataSource.setTimeBetweenEvictionRunsMillis(druidProperties.getTimeBetweenEvictionRunsMillis());
        dataSource.setMinEvictableIdleTimeMillis(druidProperties.getMinEvictableIdleTimeMillis());
        dataSource.setValidationQuery(druidProperties.getValidationQuery());
        dataSource.setTestWhileIdle(druidProperties.isTestWhileIdle());
        dataSource.setTestOnBorrow(druidProperties.isTestOnBorrow());
        dataSource.setTestOnReturn(druidProperties.isTestOnReturn());
        dataSource.setPoolPreparedStatements(druidProperties.isPoolPreparedStatements());
        dataSource.setMaxPoolPreparedStatementPerConnectionSize(druidProperties.getMaxPoolPreparedStatementPerConnectionSize());
        dataSource.setConnectionProperties(druidProperties.getConnectProperties());
        dataSource.setUseGlobalDataSourceStat(druidProperties.isUseGlobalDataSourceStat());
        try {
            dataSource.setFilters(druidProperties.getFilters());
        } catch (SQLException e) {
            log.error("createDruidDataSource {}", e);
        }
        return dataSource;
    }

    private DruidProperties createDruidProperties(String fileName, String prefix) {
        DruidProperties prop = new DruidProperties();
        prop.setUrl(ConfigGetter.getByFileItemString(fileName, "druid-primary-db-" + prefix + ".url") + "?autoReconnect=true&useUnicode=true&characterEncoding=utf-8&useSSL=false");
        prop.setUsername(ConfigGetter.getByFileItemString(fileName, "druid-primary-db-" + prefix + ".username"));
        prop.setPassword(ConfigGetter.getByFileItemString(fileName, "druid-primary-db-" + prefix + ".password"));
        prop.setInitialSize(ConfigGetter.getByFileItemInteger(fileName, "druid-primary-db-" + prefix + ".initialSize"));
        prop.setMinIdle(ConfigGetter.getByFileItemInteger(fileName, "druid-primary-db-" + prefix + ".minIdle"));
        prop.setMaxActive(ConfigGetter.getByFileItemInteger(fileName, "druid-primary-db-" + prefix + ".maxActive"));
        prop.setMaxWait(ConfigGetter.getByFileItemInteger(fileName, "druid-primary-db-" + prefix + ".maxWait"));
        prop.setTimeBetweenEvictionRunsMillis(ConfigGetter.getByFileItemInteger(fileName, "druid-primary-db-" + prefix + ".timeBetweenEvictionRunsMillis"));
        prop.setMinEvictableIdleTimeMillis(ConfigGetter.getByFileItemInteger(fileName, "druid-primary-db-" + prefix + ".minEvictableIdleTimeMillis"));
        prop.setValidationQuery(ConfigGetter.getByFileItemString(fileName, "druid-primary-db-" + prefix + ".validationQuery"));
        prop.setTestWhileIdle(ConfigGetter.getByFileItemBoolean(fileName, "druid-primary-db-" + prefix + ".testWhileIdle"));
        prop.setTestOnBorrow(ConfigGetter.getByFileItemBoolean(fileName, "druid-primary-db-" + prefix + ".testOnBorrow"));
        prop.setTestOnReturn(ConfigGetter.getByFileItemBoolean(fileName, "druid-primary-db-" + prefix + ".testOnReturn"));
        prop.setPoolPreparedStatements(ConfigGetter.getByFileItemBoolean(fileName, "druid-primary-db-" + prefix + ".poolPreparedStatements"));
        prop.setFilters(ConfigGetter.getByFileItemString(fileName, "druid-primary-db-" + prefix + ".filters"));
        return prop;
    }

    public static SqlSessionFactoryBean getSqlSessionFactoryBean(DataSource dataSource, String typeAliasPackage) {
        SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
        sqlSessionFactoryBean.setDataSource(dataSource);
        sqlSessionFactoryBean.setTypeAliasesPackage(typeAliasPackage);
        /** 设置mybatis configuration 扫描路径 */
        sqlSessionFactoryBean.setConfigLocation(new ClassPathResource(MYBATIS_CONFIG));
        return sqlSessionFactoryBean;
    }

}
