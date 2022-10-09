package mall_redis.redis_boot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;

/**
 * @EnableCaching 开启注解缓存
 */
@SpringBootApplication
@EnableCaching
public class RedisBootApplication {

    public static void main(String[] args) {
        SpringApplication.run(RedisBootApplication.class, args);
    }

}
