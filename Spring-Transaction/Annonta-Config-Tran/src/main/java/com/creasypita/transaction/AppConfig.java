package com.creasypita.transaction;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

/**
 * Created by lujq on 9/20/2022.
 */

@Configuration
@EnableTransactionManagement
public class AppConfig {

    @Bean(name = "dataSource")
    public DataSource dataSource() {
        // configure and return the necessary JDBC DataSource
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setPassword("123456");
        dataSource.setUrl("jdbc:mysql://localhost:3306/mybatis-study");
        dataSource.setUsername("root");
        return dataSource;
    }

    @Bean
    public PlatformTransactionManager txManager() {
        return new DataSourceTransactionManager(dataSource());
    }
}
