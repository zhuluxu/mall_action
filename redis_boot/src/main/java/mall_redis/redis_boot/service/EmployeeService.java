package mall_redis.redis_boot.service;

import mall_redis.redis_boot.dao.Employee;

/**
 * @author zhuluxu
 * @date 2022/10/9 11:40 下午 didi
 */
public interface EmployeeService {
    public Employee getEmployeeById(Long id);
}
