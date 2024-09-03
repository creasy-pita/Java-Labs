package com.creasypita;
import org.redisson.Redisson;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * Created by lujq on 9/3/2024.
 */
public class RedissionTest {

    private static final Logger logger = LoggerFactory.getLogger(RedissionTest.class);

    public static void main(String[] args) {

        logger.debug("aaaaaaaaaaaaa");
        // 创建配置
        Config config = new Config();
        config.useSingleServer()
                .setAddress("redis://127.0.0.1:6379")  // 替换为你的 Redis 服务器地址和端口
                .setPassword("redis@gisquest.com")
//                .setKeepAlive(true)
                .setConnectionMinimumIdleSize(1)
                .setConnectionPoolSize(10)
                .setRetryAttempts(3)
                .setRetryInterval(1500)
                .setTimeout(3000)
                .setDnsMonitoringInterval(10_000);  // 60秒检查一次连接

        // 创建 Redisson 客户端
        RedissonClient redisson = Redisson.create(config);

        try {
            // 获取 key 为 'aaaa' 的值
            RBucket<String> bucket = redisson.getBucket("aaaa");
            String value = bucket.get();

            logger.info("Key 'aaaa' 的值为: {}", value);

            // 主线程睡眠10分钟
            TimeUnit.MINUTES.sleep(10);

        } catch (InterruptedException e) {
            logger.error("主线程睡眠被中断: {}", e.getMessage());
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            logger.error("获取 Redis key 失败: {}", e.getMessage());
        } finally {
            // 关闭客户端
            redisson.shutdown();
        }
    }
}
