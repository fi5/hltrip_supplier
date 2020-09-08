package com.huoli.trip.supplier.web.config;

import com.alibaba.druid.pool.DruidDataSource;

public class DruidProperties {

	private String 		name;
	private String 		driverClassName;
	private String 		url;
	private String 		username;
	private String 		password;

	private int 		initialSize 											= DruidDataSource.DEFAULT_INITIAL_SIZE;
	private int 		minIdle 												= DruidDataSource.DEFAULT_MIN_IDLE;
	private int 		maxActive 												= DruidDataSource.DEFAULT_MAX_ACTIVE_SIZE;
	private long 		maxWait 												= DruidDataSource.DEFAULT_MAX_WAIT;
	private long 		timeBetweenEvictionRunsMillis 							= DruidDataSource.DEFAULT_TIME_BETWEEN_EVICTION_RUNS_MILLIS;
	private long 		minEvictableIdleTimeMillis 								= DruidDataSource.DEFAULT_MIN_EVICTABLE_IDLE_TIME_MILLIS;
	private String 		validationQuery 										= DruidDataSource.DEFAULT_VALIDATION_QUERY;
	private boolean 	testWhileIdle 											= DruidDataSource.DEFAULT_WHILE_IDLE;
	private boolean 	testOnBorrow 											= DruidDataSource.DEFAULT_TEST_ON_BORROW;
	private boolean 	testOnReturn 											= DruidDataSource.DEFAULT_TEST_ON_RETURN;
	private boolean 	poolPreparedStatements = false;
	private int 		maxPoolPreparedStatementPerConnectionSize = 10;
	private String 		filters;
	private String 		connectProperties;
	private boolean 	useGlobalDataSourceStat = false;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDriverClassName() {
		return driverClassName;
	}

	public void setDriverClassName(String driverClassName) {
		this.driverClassName = driverClassName;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public int getInitialSize() {
		return initialSize;
	}

	public void setInitialSize(int initialSize) {
		this.initialSize = initialSize;
	}

	public int getMinIdle() {
		return minIdle;
	}

	public void setMinIdle(int minIdle) {
		this.minIdle = minIdle;
	}

	public int getMaxActive() {
		return maxActive;
	}

	public void setMaxActive(int maxActive) {
		this.maxActive = maxActive;
	}

	public long getMaxWait() {
		return maxWait;
	}

	public void setMaxWait(long maxWait) {
		this.maxWait = maxWait;
	}

	public long getTimeBetweenEvictionRunsMillis() {
		return timeBetweenEvictionRunsMillis;
	}

	public void setTimeBetweenEvictionRunsMillis(long timeBetweenEvictionRunsMillis) {
		this.timeBetweenEvictionRunsMillis = timeBetweenEvictionRunsMillis;
	}

	public long getMinEvictableIdleTimeMillis() {
		return minEvictableIdleTimeMillis;
	}

	public void setMinEvictableIdleTimeMillis(long minEvictableIdleTimeMillis) {
		this.minEvictableIdleTimeMillis = minEvictableIdleTimeMillis;
	}

	public String getValidationQuery() {
		return validationQuery;
	}

	public void setValidationQuery(String validationQuery) {
		this.validationQuery = validationQuery;
	}

	public boolean isTestWhileIdle() {
		return testWhileIdle;
	}

	public void setTestWhileIdle(boolean testWhileIdle) {
		this.testWhileIdle = testWhileIdle;
	}

	public boolean isTestOnBorrow() {
		return testOnBorrow;
	}

	public void setTestOnBorrow(boolean testOnBorrow) {
		this.testOnBorrow = testOnBorrow;
	}

	public boolean isTestOnReturn() {
		return testOnReturn;
	}

	public void setTestOnReturn(boolean testOnReturn) {
		this.testOnReturn = testOnReturn;
	}

	public boolean isPoolPreparedStatements() {
		return poolPreparedStatements;
	}

	public void setPoolPreparedStatements(boolean poolPreparedStatements) {
		this.poolPreparedStatements = poolPreparedStatements;
	}

	public int getMaxPoolPreparedStatementPerConnectionSize() {
		return maxPoolPreparedStatementPerConnectionSize;
	}

	public void setMaxPoolPreparedStatementPerConnectionSize(int maxPoolPreparedStatementPerConnectionSize) {
		this.maxPoolPreparedStatementPerConnectionSize = maxPoolPreparedStatementPerConnectionSize;
	}

	public String getFilters() {
		return filters;
	}

	public void setFilters(String filters) {
		this.filters = filters;
	}

	public String getConnectProperties() {
		return connectProperties;
	}

	public void setConnectProperties(String connectProperties) {
		this.connectProperties = connectProperties;
	}

	public boolean isUseGlobalDataSourceStat() {
		return useGlobalDataSourceStat;
	}

	public void setUseGlobalDataSourceStat(boolean useGlobalDataSourceStat) {
		this.useGlobalDataSourceStat = useGlobalDataSourceStat;
	}
}
