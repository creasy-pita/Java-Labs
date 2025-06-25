package com.creasypita.utils;

import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by lujq on 3/14/2025.
 */
public class MyDbUtil {

    private SqlSessionFactory sqlSessionFactory;

    public MyDbUtil(String resource){
        InputStream inputStream = null;
        try {
            inputStream = Resources.getResourceAsStream(resource);
        } catch (IOException e) {
            e.printStackTrace();
        }
        sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
    }

    public MyDbUtil(String resource, String dbUrl, String username, String password){
        InputStream inputStream = null;
        try {
            inputStream = Resources.getResourceAsStream(resource);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Properties properties = new Properties();
        //可从外部配置项动态设置reg3 gisqbpm的模式名
        String schema = System.getProperty("reg3.gisqbpm.schema");
        if (StringUtils.isNotBlank(schema)) {
            properties.setProperty("reg3gisqbpmSchemaName", schema);
        }
        String databaseId = "mysql";
        if (dbUrl.contains("jdbc:postgres")) {
            databaseId = "postgres";
        } else if (dbUrl.contains("jdbc:oracle")){
            databaseId = "oracle";
        }
        properties.setProperty("jdbc.url", dbUrl);
        properties.setProperty("jdbc.username", username);
        properties.setProperty("jdbc.password", password);
        sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream, properties);
        sqlSessionFactory.getConfiguration().setDatabaseId(databaseId);
    }

    /**
     * 事务不会自动提交，注意需要手动commit try(openSession()){} 语法不会执行commit()
     * @return
     */
    public SqlSession openSession(){
        return this.sqlSessionFactory.openSession();
    }

    public SqlSession openSession(boolean autoCommit){
        return this.sqlSessionFactory.openSession(autoCommit);
    }
}

