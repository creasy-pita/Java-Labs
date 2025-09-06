package com.creasypita.jdbc;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Properties配置文件读取工具类
 * 支持从类路径或文件系统加载properties文件
 */
public class ConfigProperties {
    private Properties properties;

    /**
     * 构造函数 - 从类路径加载properties文件
     * @param fileName properties文件名（类路径下）
     */
    public ConfigProperties(String fileName) {
        this.properties = new Properties();
        loadFromClasspath(fileName);
//        loadPropertiesFile(fileName);
    }

    /**
     * 从类路径加载properties文件
     * @param fileName properties文件名
     */
    private void loadFromClasspath(String fileName) {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(fileName)) {
            if (inputStream == null) {
                throw new RuntimeException("配置文件未找到: " + fileName);
            }
            properties.load(inputStream);
            System.out.println("配置文件加载成功: " + fileName);
        } catch (IOException e) {
            throw new RuntimeException("加载配置文件时出错: " + fileName, e);
        }
    }

    public void loadPropertiesFile(String filePath) {
        Properties props = new Properties();
        try (InputStream input = new FileInputStream(filePath)) {
            props.load(input);
        } catch (FileNotFoundException e) {
            // 文件不存在，返回空Properties对象
            this.properties = new Properties();
        } catch (IOException e) {
            System.err.println("加载properties文件时出错: " + e.getMessage());
            this.properties =  new Properties();
        }
        this.properties =  props;
    }

    /**
     * 获取字符串类型的配置值
     * @param key 配置键
     * @return 配置值，如果不存在则返回null
     */
    public String getString(String key) {
        return properties.getProperty(key);
    }

    /**
     * 获取字符串类型的配置值，带默认值
     * @param key 配置键
     * @param defaultValue 默认值
     * @return 配置值，如果不存在则返回默认值
     */
    public String getString(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    /**
     * 获取整数类型的配置值
     * @param key 配置键
     * @return 配置值，如果不存在或格式错误则返回null
     */
    public Integer getInt(String key) {
        String value = properties.getProperty(key);
        if (value == null) {
            return null;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            System.err.println("配置项 '" + key + "' 的值 '" + value + "' 不是有效的整数");
            return null;
        }
    }

    /**
     * 获取整数类型的配置值，带默认值
     * @param key 配置键
     * @param defaultValue 默认值
     * @return 配置值，如果不存在或格式错误则返回默认值
     */
    public int getInt(String key, int defaultValue) {
        Integer value = getInt(key);
        return value != null ? value : defaultValue;
    }

    /**
     * 获取布尔类型的配置值
     * @param key 配置键
     * @return 配置值，如果不存在则返回null
     */
    public Boolean getBoolean(String key) {
        String value = properties.getProperty(key);
        if (value == null) {
            return null;
        }
        return Boolean.parseBoolean(value.trim());
    }

    /**
     * 获取布尔类型的配置值，带默认值
     * @param key 配置键
     * @param defaultValue 默认值
     * @return 配置值，如果不存在则返回默认值
     */
    public boolean getBoolean(String key, boolean defaultValue) {
        Boolean value = getBoolean(key);
        return value != null ? value : defaultValue;
    }

    /**
     * 检查配置项是否存在
     * @param key 配置键
     * @return 如果存在返回true，否则返回false
     */
    public boolean containsKey(String key) {
        return properties.containsKey(key);
    }

    /**
     * 获取所有配置属性
     * @return Properties对象
     */
    public Properties getProperties() {
        return properties;
    }

    /**
     * 示例用法
     */
    public static void main(String[] args) {
        // 创建配置对象（假设有config.properties文件在类路径下）
        ConfigProperties config = new ConfigProperties("config.properties");

        // 读取字符串配置
        String dbUrl = config.getString("database.url");
        String dbUser = config.getString("database.user", "admin"); // 带默认值

        // 读取整数配置
        Integer port = config.getInt("server.port");
        int timeout = config.getInt("server.timeout", 30); // 带默认值

        // 读取布尔配置
        Boolean enabled = config.getBoolean("feature.enabled");
        boolean debugMode = config.getBoolean("app.debug", false); // 带默认值

        // 输出读取的配置
        System.out.println("数据库URL: " + dbUrl);
        System.out.println("数据库用户: " + dbUser);
        System.out.println("服务器端口: " + port);
        System.out.println("超时时间: " + timeout);
        System.out.println("功能是否启用: " + enabled);
        System.out.println("调试模式: " + debugMode);
    }
}
