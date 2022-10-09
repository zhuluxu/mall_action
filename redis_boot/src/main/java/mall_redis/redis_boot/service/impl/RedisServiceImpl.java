package mall_redis.redis_boot.service.impl;

import mall_redis.redis_boot.service.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author zhuluxu
 * @date 2022/10/9 8:13 下午 didi
 */
@Repository
public class RedisServiceImpl implements RedisService {

    @Resource
    private RedisTemplate redisTemplate;



}
