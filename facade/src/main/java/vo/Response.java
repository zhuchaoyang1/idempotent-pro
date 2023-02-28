package vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author zcy
 * @date 2023/2/23
 * @description 通用响应结果集
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Response<T> implements java.io.Serializable {

    private T data;

    private String code;

    private String errMsg;

    public static <T> Response ok(T t) {
        return Response.builder().code("1").errMsg("OK").data(t).build();
    }

    public static <T> Response error(T t) {
        return Response.builder().code("0").errMsg("error").data(t).build();
    }

    public static <T> Response repeat(T t) {
        return Response.builder().code("-1").errMsg("repeat").data(t).build();
    }

    @Override
    public String toString() {
        return "Code：" + code + "，ErrMsg：" + errMsg + "，Data：" + data;
    }

}
