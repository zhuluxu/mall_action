package mall_redis.redis_boot.service.impl;

import mall_redis.redis_boot.dao.Employee;
import mall_redis.redis_boot.service.EmployeeService;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * @author zhuluxu
 * @date 2022/10/9 11:41 下午 didi
 */
@Service
public class EmployeeServiceImpl implements EmployeeService {

    @Override
    @Cacheable(cacheNames = "empls", keyGenerator = "myKeyGenerator")
    public Employee getEmployeeById(Long id) {
        return new Employee(id);
    }
}
