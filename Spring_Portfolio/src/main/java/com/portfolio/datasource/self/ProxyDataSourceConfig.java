package com.portfolio.datasource.self;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jndi.JndiObjectFactoryBean;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.portfolio.datasource.DataSourceConstraints;
import com.portfolio.uitls.LogUtils;

import lombok.RequiredArgsConstructor;
import net.ttddyy.dsproxy.support.ProxyDataSourceBuilder;

/*
 * 代理資料來源配置類
 * 支持 Local (JDBC) 與 Server (JNDI) 環境切換
 */
@Configuration
@EnableJpaRepositories(
		basePackages = "com.portfolio.repository",
		entityManagerFactoryRef = "proxyEntityManager",
		transactionManagerRef = "proxyTransactionManager"
)
@EnableTransactionManagement
@RequiredArgsConstructor
public class ProxyDataSourceConfig {

	private final Environment env;

	@Value("${spring.datasource.jndi-name:java:comp/env/jdbc/tdep_ds}")
	private String jndiName;

	@Primary
	@Bean(name = "proxyDataSource")
	public DataSource proxyDataSource() {
		LogUtils.debug("ProxyDataSourceConfig: Initializing DataSource...");

		DataSource baseDataSource;
		String jdbcUrl = env.getProperty("spring.datasource.url");

		// 判斷是否為 Local 環境 (檢查是否有 JDBC URL)
		if (jdbcUrl != null && !jdbcUrl.isEmpty()) {
			LogUtils.debug("Mode: Local JDBC Connection Detected");
			baseDataSource = DataSourceBuilder.create()
					.url(jdbcUrl)
					.username(env.getProperty("spring.datasource.username"))
					.password(env.getProperty("spring.datasource.password"))
					.driverClassName(env.getProperty("spring.datasource.driver-class-name"))
					.build();
		} else {
			// Server 環境：走 JNDI 查找
			LogUtils.debug("Mode: JNDI Lookup Detected (" + jndiName + ")");
			JndiObjectFactoryBean jndiBean = new JndiObjectFactoryBean();
			jndiBean.setJndiName(jndiName);
			jndiBean.setProxyInterface(DataSource.class);
			jndiBean.setLookupOnStartup(false);
			try {
				jndiBean.afterPropertiesSet();
				baseDataSource = (DataSource) jndiBean.getObject();
			} catch (Exception e) {
				throw new RuntimeException("Failed to initialize JNDI DataSource. Check your server context or properties.", e);
			}
		}

		// 包裝 Proxy 代理，用於 SQL 監控與日誌
		return ProxyDataSourceBuilder.create(baseDataSource)
				.name(DataSourceConstraints.PROXY_DATA_SOURCE_NAME)
				.listener(new ProxyDatasourceQueryExecutionListener())
				.asJson()
				.countQuery()
				.build();
	}

	/*
	 * 實體管理器工廠
	 */
	@Primary
	@Bean(name = "proxyEntityManager")
	public LocalContainerEntityManagerFactoryBean proxyEntityManager() {
		LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
		em.setDataSource(proxyDataSource());
		em.setPackagesToScan(DataSourceConstraints.SELF_ENTITIES_PACKAGE);

		HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
		em.setJpaVendorAdapter(vendorAdapter);

		// 讀取 Hibernate 屬性
		Map<String, Object> properties = new HashMap<>();
		safePut(properties, DataSourceConstraints.HIBERNATE_GENERATE_SQL);
		safePut(properties, DataSourceConstraints.HIBERNATE_DIALECT);
		safePut(properties, DataSourceConstraints.HIBERNATE_SHOW_SQL);
		safePut(properties, DataSourceConstraints.HIBERNATE_FORMAT_SQL);

		// 如果是在 Local 且沒抓到 Dialect，手動強制指定防止 Hibernate 報錯
		if (properties.get(DataSourceConstraints.HIBERNATE_DIALECT) == null) {
			properties.put("hibernate.dialect", "org.hibernate.dialect.OracleDialect");
		}

		em.setJpaPropertyMap(properties);
		return em;
	}

	/*
	 * 交易管理器
	 * 透過參數注入 proxyEntityManager，確保 Spring Bean 生命週期一致
	 */
	@Primary
	@Bean(name = "proxyTransactionManager")
	public PlatformTransactionManager proxyTransactionManager(
			@Qualifier("proxyEntityManager") LocalContainerEntityManagerFactoryBean entityManagerFactory) {
		JpaTransactionManager transactionManager = new JpaTransactionManager();
		transactionManager.setEntityManagerFactory(entityManagerFactory.getObject());
		return transactionManager;
	}

	/**
	 * 安全讀取 Environment 屬性
	 */
	private void safePut(Map<String, Object> map, String key) {
		String value = env.getProperty(key);
		if (value != null) {
			map.put(key, value);
		}
	}
}