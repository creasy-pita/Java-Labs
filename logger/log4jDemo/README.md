## 下面是一个简单的 log4j 配置和使用示例：

### 添加 log4j 依赖

首先需要在项目中添加 log4j 依赖，可以在 Maven 项目中添加以下依赖：

```xml
<dependency>
<groupId>log4j</groupId>
<artifactId>log4j</artifactId>
<version>1.2.17</version>
</dependency>
```

### 配置 log4j

在项目的 classpath 中添加一个名为 log4j.properties 的配置文件，其中可以指定 log4j 的日志输出方式、级别和格式等。

```properties
# 设置 rootLogger 的输出级别为 info，同时配置 Console 输出

log4j.rootLogger=INFO, Console

# 配置 Console 输出的格式

log4j.appender.Console=org.apache.log4j.ConsoleAppender
log4j.appender.Console.layout=org.apache.log4j.PatternLayout
log4j.appender.Console.layout.ConversionPattern=%d{yyyy-MM-dd HH:mm:ss} %-5p %c{1}:%L - %m%n
```

#### 使用 log4j

在代码中使用 log4j 记录日志：

```java
import org.apache.log4j.Logger;

public class LogTest {
// 创建 logger 实例
private static final Logger logger = Logger.getLogger(LogTest.class);

    public static void main(String[] args) {
        logger.debug("Debug log");
        logger.info("Info log");
        logger.warn("Warn log");
        logger.error("Error log");
        logger.fatal("Fatal log");
    }
}
```

### 其他

在上面的示例中，我们创建了一个 logger 实例，通过调用 logger 的 debug、info、warn、error 和 fatal 等方法，记录不同级别的日志。日志将根据配置文件中的设置输出到控制台或其他地方。

需要注意的是，在实际开发中，通常会根据不同的应用场景和需求，更加细致地配置 log4j，例如设置输出文件路径、文件大小、日志滚动等。


## log4j.rootLogger配置不同输出端使用不同的日志级别

比如 配置实现 log4j.rootLogger 同时设置 Console 输出 info 级别，RollingFile 输出 debug 级别

```properties
# 设置 rootLogger 的输出级别为 debug，同时配置 Console 和 RollingFile 输出
log4j.rootLogger=DEBUG, Console, RollingFile

# 配置 Console 输出的格式和级别为 info
log4j.appender.Console=org.apache.log4j.ConsoleAppender
log4j.appender.Console.layout=org.apache.log4j.PatternLayout
log4j.appender.Console.layout.ConversionPattern=%d{HH:mm:ss} %-5p [%t] %c{2} - %m%n
log4j.appender.Console.Threshold=INFO

# 配置 RollingFile 输出的格式和级别为 debug
log4j.appender.RollingFile=org.apache.log4j.RollingFileAppender
log4j.appender.RollingFile.File=/path/to/your/log/file.log
log4j.appender.RollingFile.MaxFileSize=5MB
log4j.appender.RollingFile.MaxBackupIndex=10
log4j.appender.RollingFile.layout=org.apache.log4j.PatternLayout
log4j.appender.RollingFile.layout.ConversionPattern=%d{HH:mm:ss} %-5p [%t] %c{2} - %m%n
log4j.appender.RollingFile.Threshold=DEBUG

```

上面的配置中，log4j.rootLogger 设置了 DEBUG 级别，同时配置了 Console 和 RollingFile 两个 Appender 的输出。Console 的输出级别设置为 INFO，RollingFile 的输出级别设置为 DEBUG。

在配置中，可以根据实际需求修改 Console 和 RollingFile 的输出格式、路径、文件大小等参数。需要注意的是，在同时输出到 Console 和 RollingFile 时，由于 Console 会将日志实时输出到控制台，因此建议将 Console 输出级别设置为较高的级别，例如 INFO 或 WARN，以避免大量无用的 debug 日志输出到控制台上造成干扰。