package com.creasypita.transaction;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import javax.sql.DataSource;

/**
 * Created by lujq on 9/21/2022.
 */
@Configuration
@MapperScan(basePackages = {"com.creasypita.transaction.mappers"}
        , sqlSessionFactoryRef = "sqlSessionFactory")
public class MapperConfig {
    @Bean(name = "sqlSessionFactory")
    public SqlSessionFactory sqlSessionFactory(DataSource dataSource)
            throws Exception {
        final SqlSessionFactoryBean sessionFactory = new SqlSessionFactoryBean();
        sessionFactory.setDataSource(dataSource);
        Resource[] daoRes = new PathMatchingResourcePatternResolver().getResources("classpath:mybatis/*.xml");
        Resource[] resources = ArrayUtils.addAll(daoRes);
        sessionFactory.setMapperLocations(resources);
        return sessionFactory.getObject();
    }

}