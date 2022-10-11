package mall_redis.redis_boot.exception;

/**
 * @author zhuluxu
 * @date 2022/10/10 5:52 下午
 */

public class CreateException extends RuntimeException {

    public CreateException() {
    }

    public CreateException(String message) {
        super(message);
    }

    public CreateException(Throwable cause) {
        super(cause);
    }

    public CreateException(String message, Throwable cause) {
        super(message, cause);
    }

}
