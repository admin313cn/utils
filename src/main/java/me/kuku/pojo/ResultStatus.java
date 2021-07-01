package me.kuku.pojo;

import lombok.Getter;
import lombok.ToString;

@ToString
@Getter
public enum ResultStatus {
	SUCCESS(200, "成功"),
	FAIL(500, "失败"),
	PARAM_ERROR(501, "参数异常"),
	DATA_EXISTS(502, "数据已存在"),
	DATA_NOT_EXISTS(503, "数据不存在"),
	NOT_SCANNED(504, "二维码未扫描"),
	EXPIRED(505, "二维码已失效"),
	CONFIG_ERROR(506, "配置文件错误"),
	SCANNED(507, "二维码已扫描"),
	COOKIE_EXPIRED(508, "cookie已失效");


	private final Integer code;
	private final String message;

	ResultStatus(Integer code, String message){
		this.code = code;
		this.message = message;
	}
}
