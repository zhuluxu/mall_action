package mall_redis.redis_boot.config;

import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * @author zhuluxu
 * @date 2022/10/9 11:36 下午 didi
 */
@Configuration
public class KeyGeneratorConfig {

    @Bean("myKeyGenerator")
    public KeyGenerator keyGenerator() {
        return new KeyGenerator() {
            @Override
            public Object generate(Object target, Method method, Object... params) {
                return method.getName() + "["+ Arrays.asList(params).toString()+"]";
            }
        };
    }
}
