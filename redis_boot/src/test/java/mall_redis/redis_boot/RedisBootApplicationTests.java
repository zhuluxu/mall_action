package mall_redis.redis_boot;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import mall_redis.redis_boot.dao.Employee;
import org.junit.Test;
//import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.DataInput;
import java.io.IOException;

@SpringBootTest
class RedisBootApplicationTests {

    @Test
    void contextLoads() {
    }

    public static void main(String[] args) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        Employee employee = new Employee();
        employee.setId(12L);
//        employee.setName("诸葛孔明");
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        objectMapper.enableDefaultTyping(ObjectMapper.DefaultTyping.OBJECT_AND_NON_CONCRETE, JsonTypeInfo.As.PROPERTY);
//        String emp = "{\"id\":12,\"name\":\"诸葛孔明\"}";
        String emp = "[\"mall_redis.redis_boot.dao.Employee\",{\"id\":12,\"name\":\"诸葛孔明\"}]";
        System.out.println(objectMapper.valueToTree(employee));
//        System.out.println(objectMapper.readValue(emp, Employee.class));
//        System.out.println(objectMapper.readValue(emp, Employee.class));;
//        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
//        objectMapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.WRAPPER_OBJECT);
//        // 将当前对象的数据类型也存入序列化的结果字符串中，以便反序列化
////        objectMapper.activateDefaultTyping(LaissezFaireSubTypeValidator.instance, ObjectMapper.DefaultTyping.NON_FINAL);
//        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
//        //处理不同的时区偏移格式
//        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
//        objectMapper.registerModule(new JavaTimeModule());
//
//        String emp = "{\"name\":\"诸葛孔明\"}";
//
//        Employee employee = objectMapper.readValue(emp, Employee.class);
//        System.out.println(employee);

    }

}
