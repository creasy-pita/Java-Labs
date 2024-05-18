package com.creasypita.logger;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configurator;

import java.io.IOException;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * log4j动态监听和热更新日志级别
 * 具体描述
 * log4j动态监听指定文件中的日志级别的配置，检测到更新后实时更新日志级别，并测试效果
 */
public class ConfigFileWatcher {

    private static final Logger logger = LogManager.getLogger(ConfigFileWatcher.class);

    private final Path configFile;
    private final Map<String, String> currentConfig;
    private final ConfigChangeListener listener;

    public ConfigFileWatcher(String configFilePath, ConfigChangeListener listener) throws IOException {
        this.configFile = Paths.get(configFilePath);
        this.listener = listener;
        this.currentConfig = loadConfig();
        watchConfigFile();
    }

    private Map<String, String> loadConfig() throws IOException {
        Properties properties = new Properties();
        properties.load(Files.newInputStream(configFile));
        Map<String, String> configMap = new HashMap<>();
        properties.forEach((key, value) -> configMap.put((String) key, (String) value));
        return configMap;
    }

    private void watchConfigFile() throws IOException {
        WatchService watchService = FileSystems.getDefault().newWatchService();
        configFile.getParent().register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);

        new Thread(() -> {
            try {
                while (true) {
                    WatchKey key = watchService.take();
                    for (WatchEvent<?> event : key.pollEvents()) {
                        if (event.kind() == StandardWatchEventKinds.ENTRY_MODIFY) {
                            Path modifiedFile = (Path) event.context();
                            if (modifiedFile.equals(configFile.getFileName())) {
                                Map<String, String> newConfig = loadConfig();
                                Map<String, String> diff = getDiff(currentConfig, newConfig);
                                currentConfig.clear();
                                currentConfig.putAll(newConfig);
                                listener.onConfigChange(diff);
                            }
                        }
                    }
                    key.reset();
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private Map<String, String> getDiff(Map<String, String> oldConfig, Map<String, String> newConfig) {
        Map<String, String> diff = new HashMap<>();
        for (Map.Entry<String, String> entry : newConfig.entrySet()) {
            String key = entry.getKey();
            String newValue = entry.getValue();
            String oldValue = oldConfig.get(key);
            if (oldValue == null || !oldValue.equals(newValue)) {
                diff.put(key, newValue);
            }
        }
        return diff;
    }

    public interface ConfigChangeListener {
        void onConfigChange(Map<String, String> changedProperties);
    }

    private static void updateLogLevels(String loggerPath, String logLevel) {
        // 设置log4j的日志级别
        Configurator.setLevel(loggerPath, org.apache.logging.log4j.Level.valueOf(logLevel));

    }

    private static void updateLogLevels2(String loggerPath, String logLevel) {
        // 设置log4j的日志级别
        LoggerContext context = LoggerContext.getContext(false);


    }

    // Example usage:
    public static void main(String[] args) throws IOException {
        ConfigFileWatcher watcher = new ConfigFileWatcher("E:\\work\\myproject\\java\\Java-Labs\\logger\\log4j2Demo\\target\\classes\\config.properties", changedProperties -> {
            System.out.println("Config properties changed:");
            changedProperties.forEach((key, value) ->
                {
                    System.out.println(key + " = " + value);
                    updateLogLevels(key, value);
                    System.out.println("---------------------------------------------------------");
                    logger.info("info");
                    logger.debug("debug");
                }
            );


        });

        // Keep the program running
        try {
            Thread.sleep(Long.MAX_VALUE);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

