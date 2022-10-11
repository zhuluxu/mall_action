package mall_redis.redis_boot.tool;

import io.lettuce.core.RedisClient;
import mall_redis.redis_boot.exception.CreateException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.view.tiles3.SpringWildcardServletTilesApplicationContext;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.concurrent.TimeUnit.*;

/**
 * @author zhuluxu
 * @date 2022/10/10 5:53 下午 didi
 */

public class RedLimiter {

    private RedisTemplate<String, Object> redisTemplate;

    private static final String STORED_PERMITS = "storedPermits";
    private static final String MAX_PERMITS = "maxPermits";
    private static final String STABLE_INTERVAL_MICROS = "stableIntervalMicros";
    private static final String NEXT_FREE_TICKET_MICROS = "nextFreeTicketMicros";

    private static final String SCRIPT_LUA = "lua/RedLimiter.lua";
    private static final String SCRIPT = readScript();

    private static final ConcurrentMap<String, RedLimiter> LIMITERS = new ConcurrentHashMap<>();

    private final String key;
    private double qps;
    private String sha1;
    private volatile int batchSize = 100;
    private volatile long lastMillis = 0L;
    private volatile long batchInterval = 100L;

    private AtomicInteger qpsHolder = new AtomicInteger(0);

    private RedLimiter(String key, double qps, RedisTemplate<String, Object> redisTemplate, boolean setProperties) {
        this.key = key;
        this.qps = qps;
        this.redisTemplate = redisTemplate;
        if (redisTemplate == null ) {
            throw new CreateException("no redisTemplate");
        }
        if (setProperties) {
            setProperties();
        }
//        loadScriptSha1();
    }



    private void loadScriptSha1() {
        // 执行 lua 脚本
        DefaultRedisScript<String> redisScript = new DefaultRedisScript<>();
        // 指定 lua 脚本
        redisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("lua/RedLimiter.lua")));
        // 指定返回类型
        redisScript.setResultType(String.class);
        // 参数一：redisScript，参数二：key列表，参数三：arg（可多个）
        this.sha1 = redisTemplate.execute(redisScript, Collections.singletonList(key));

        // 指定 lua 脚本，并且指定返回值类型
//        DefaultRedisScript<String> redisScript = new DefaultRedisScript<>(SCRIPT,String.class);
//        // 参数一：redisScript，参数二：key列表，参数三：arg（可多个）
//        Long result = redisTemplate.execute(redisScript, Collections.singletonList(key));
    }

    private static String readScript() {
        InputStream is = RedLimiter.class.getClassLoader().getResourceAsStream(SCRIPT_LUA);
        Objects.requireNonNull(is);
        StringBuilder builder = new StringBuilder();
        try {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    builder.append(line);
                    builder.append("\n");
                }
            }
        } catch (IOException e) {
            // will not reach here
        }
        return builder.toString();
    }

    private void setProperties() {
        Map<String, String> limiter = new HashMap<>();
        //当前存储的令牌数
        limiter.put(STORED_PERMITS, Double.toString(qps));
        //最大可存储的令牌数，设置为限速器的qps
        limiter.put(MAX_PERMITS, Double.toString(qps));
        //stableIntervalMicros 多久产生一个令牌
        limiter.put(STABLE_INTERVAL_MICROS, Double.toString(TimeUnit.SECONDS.toMicros(1L) / qps));
        //nextFreeTicketMicros 下一次可以获取令牌的时间点  允许补充令牌的时间戳
        limiter.put(NEXT_FREE_TICKET_MICROS, "0");
        redisTemplate.opsForHash().putAll(key, limiter);
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    public void setBatchInterval(long batchInterval) {
        this.batchInterval = batchInterval;
    }

    public double acquire() {
        return acquire(1D);
    }

    public double acquireLazy(int batchQps) {
        qpsHolder.addAndGet(batchQps);
        long currentMillis = System.currentTimeMillis();
        if (qpsHolder.get() >= batchSize || (currentMillis - this.lastMillis) >= batchInterval) {
            int qps = qpsHolder.getAndSet(0);
            this.lastMillis = currentMillis;
            return acquire(qps);
        } else {
            return 0D;
        }
    }

    public double acquire(double qps) {
        long nowMicros = MILLISECONDS.toMicros(System.currentTimeMillis());
        long waitMicros = 0;
        if (redisTemplate != null) {
            // 指定 lua 脚本，并且指定返回值类型
            DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>(SCRIPT,Long.class);
            // 参数一：redisScript，参数二：key列表，参数三：arg（可多个）
            Long result = redisTemplate.execute(redisScript, Collections.singletonList(key), "acquire",
                    Double.toString(qps), Long.toString(nowMicros));
            waitMicros=result.longValue();
        }
        double wait = 1.0 * waitMicros / SECONDS.toMicros(1L);
        if (waitMicros > 0) {
            sleepUninterruptibly(waitMicros, MICROSECONDS);
        }
        return wait;
    }

    public boolean tryAcquire(double qps, long timeout, TimeUnit unit) {
        long nowMicros = MILLISECONDS.toMicros(System.currentTimeMillis());
        long timeoutMicros = unit.toMicros(timeout);
        long waitMicros = 0;
        if (redisTemplate != null) {
            // 指定 lua 脚本，并且指定返回值类型
            DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>(SCRIPT,Long.class);
            // 参数一：redisScript，参数二：key列表，参数三：arg（可多个）
            Long result = redisTemplate.execute(redisScript, Collections.singletonList(key), "tryAcquire",
                    Double.toString(qps), Long.toString(nowMicros), Long.toString(timeoutMicros));
            waitMicros=result.longValue();
        }
        if (waitMicros < 0) {
            return false;
        }
        if (waitMicros > 0) {
            sleepUninterruptibly(waitMicros, MICROSECONDS);
        }
        return true;
    }

    // from Guava Uninterruptibles
    private static void sleepUninterruptibly(long sleepFor, TimeUnit unit) {
        boolean interrupted = false;
        try {
            long remainingNanos = unit.toNanos(sleepFor);
            long end = System.nanoTime() + remainingNanos;
            while (true) {
                try {
                    // TimeUnit.sleep() treats negative timeouts just like zero.
                    NANOSECONDS.sleep(remainingNanos);
                    return;
                } catch (InterruptedException e) {
                    interrupted = true;
                    remainingNanos = end - System.nanoTime();
                }
            }
        } finally {
            if (interrupted) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public static RedLimiter create(String key, double qps, RedisTemplate redisTemplate) {
        return create(key, qps, redisTemplate, false);
    }

    public static RedLimiter create(String key, double qps, RedisTemplate redisTemplate, boolean setProperties) {
        return LIMITERS.computeIfAbsent(key, k -> new RedLimiter(k, qps, redisTemplate,  setProperties));
    }


    public void setRate(double qps) {
        this.qps = qps;
        setProperties();
    }


}
