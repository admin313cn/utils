package me.kuku.pojo;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

@Data
public class HiToKoTo {
	private Integer id;
	private String uuid;
	private String hitokoto;
	private String type;
	private String from;
	@JSONField(name = "from_who")
	private String fromWho;
	private String creator;
	@JSONField(name = "creator_uid")
	private Integer creatorUid;
	private Integer reviewer;
	@JSONField(name = "commit_from")
	private String commitFrom;
	@JSONField(name = "created_at")
	private Long createdAt;
	private Integer length;
}
