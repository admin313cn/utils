package me.kuku.pojo;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Getter;
import lombok.ToString;

import java.util.HashMap;
import java.util.Map;

@Getter
@ToString
public class Result<T> {
    private final Integer code;
    private final String message;
    private final T data;

    private Result(Integer code, String message, T data){
        this.code = code;
        this.message = message;
        this.data = data;
    }

    @JSONField(serialize = false)
    public boolean isSuccess(){
        return this.code == 200;
    }

    @JSONField(serialize = false)
    public boolean isFailure(){
        return this.code != 200;
    }

    public static Result<Void> success(){
        return new Result<>(200, "成功", null);
    }

    public static <T> Result<T> success(T data){
        return new Result<>(200, "成功", data);
    }

    public static <T> Result<T> success(String message, T data){
        if (message == null){
            return success(data);
        }
        return new Result<>(200, message, data);
    }

    public static Map<String, Object> map(Object value){
        return map("data", value);
    }

    public static Map<String, Object> map(String key, Object value){
        return new HashMap<String, Object>(){{
            put(key, value);
        }};
    }

    public static Map<String, String> map(String key, String value){
        return new HashMap<String, String>(){{
            put(key, value);
        }};
    }

    public static Map<String, String> map(String value){
        return map("data", value);
    }

    public static <T> Result<T> failure(String message){
        return new Result<>(500, message, null);
    }

    public static <T> Result<T> failure(T data){
        return failure(500, "", data);
    }

    public static <T> Result<T> failure(Integer code, String message, T data){
        return new Result<>(code, message, data);
    }

    public static <T> Result<T> failure(Integer code, String message){
        return new Result<>(code, message, null);
    }

    public static <T> Result<T> failure(String message, T data){
        return failure(500, message, data);
    }

    public static <T> Result<T> failure(ResultStatus resultStatus, T data){
        return failure(resultStatus.getCode(), resultStatus.getMessage(), data);
    }

    public static <T> Result<T> failure(ResultStatus resultStatus){
        return failure(resultStatus, null);
    }

    public boolean equals(ResultStatus resultStatus){
        return this.code.equals(resultStatus.getCode());
    }

}
