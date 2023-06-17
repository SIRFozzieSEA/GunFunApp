package com.codef.gunfunapp;

import java.text.SimpleDateFormat;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableJpaRepositories(basePackages = { "com.codef.gunfunapp.models.entities", "com.codef.gunfunapp.repos" })
@PropertySource("classpath:application.properties")
@EnableTransactionManagement
public class GunFunConfiguration {

	public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
	public static final SimpleDateFormat DATE_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	@Bean(name = "dsMaster")
	@Primary
	@ConfigurationProperties(prefix = "spring.datasource")
	public DataSource masterDataSource() {
		return DataSourceBuilder.create().build();
	}

	@Bean(name = "jdbcMaster")
	public JdbcTemplate masterJdbcTemplate(@Qualifier("dsMaster") DataSource dsMaster) {
		return new JdbcTemplate(dsMaster);
	}

//	@Bean(name = "jdbcSlave")
//	public JdbcTemplate slaveJdbcTemplate(@Qualifier("dsSlave") DataSource dsSlave) {
//		return new JdbcTemplate(dsSlave);
//	}
//
//	@Bean(name = "dsSlave")
//	@ConfigurationProperties(prefix = "spring.datasource-two")
//	public DataSource slaveDataSource() {
//		return DataSourceBuilder.create().build();
//	}

//	@Bean
//	public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
//		final LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
//		em.setDataSource(dataSource());
//		em.setPackagesToScan(new String[] { "com.codef.fourchanhelper" });
//		em.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
//		em.setJpaProperties(additionalProperties());
//		return em;
//	}

//	@Bean
//	JpaTransactionManager transactionManager(final EntityManagerFactory entityManagerFactory) {
//		final JpaTransactionManager transactionManager = new JpaTransactionManager();
//		transactionManager.setEntityManagerFactory(entityManagerFactory);
//		return transactionManager;
//	}

//	final Properties additionalProperties() {
//		final Properties hibernateProperties = new Properties();
//		hibernateProperties.setProperty("hibernate.hbm2ddl.auto", env.getProperty("hibernate.hbm2ddl.auto"));
//		hibernateProperties.setProperty("hibernate.dialect", env.getProperty("hibernate.dialect"));
//		hibernateProperties.setProperty("hibernate.show_sql", env.getProperty("hibernate.show_sql"));
//		hibernateProperties.setProperty("hibernate.format_sql", env.getProperty("hibernate.format_sql"));
//		return hibernateProperties;
//	}

}
