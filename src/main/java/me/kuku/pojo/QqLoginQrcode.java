package me.kuku.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class QqLoginQrcode {
	private byte[] bytes;
	private String sig;
}
