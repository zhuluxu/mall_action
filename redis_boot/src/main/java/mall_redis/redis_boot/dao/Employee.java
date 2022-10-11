package mall_redis.redis_boot.dao;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author zhuluxu
 * @date 2022/10/9 11:41 下午 didi
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Employee {
    private Long id;
    public String name;
    private final String nameFinal = "final";
}
