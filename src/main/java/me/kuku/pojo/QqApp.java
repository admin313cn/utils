package me.kuku.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class QqApp {
	private Long appId;
	private Integer daId;
	private Long ptAid = 0L;

	public QqApp(Long appId, Integer daId){
		this.appId = appId;
		this.daId = daId;
	}
}
