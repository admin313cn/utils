package me.kuku.pojo;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import me.kuku.utils.QqUtils;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class QqLoginPojo {
	private Long qq;
	private String sKey;
	private String psKey;
	private String superKey;
	private String superToken;
	private String pt4Token;

	@JSONField(serialize = false)
	public String getCookie(){
		return String.format("pt2gguin=o0%s; uin=o0%s; skey=%s; ", qq, qq, sKey);
	}

	@JSONField(serialize = false)
	public String getCookie(String psKey){
		return String.format("%sp_skey=%s; p_uin=o0%s;", getCookie(), psKey, qq);
	}

	@JSONField(serialize = false)
	public String getCookieWithPs(){
		return String.format("%sp_skey=%s; p_uin=o0%s; ", getCookie(), psKey, qq);
	}

	@JSONField(serialize = false)
	public String getCookieWithSuper(){
		return String.format("superuin=o0%s; superkey=%s; supertoken=%s; ", qq, superKey, superToken);
	}

	@JSONField(serialize = false)
	public String getGtk(){
		return String.valueOf(QqUtils.getGTK(sKey));
	}

	@JSONField(serialize = false)
	public String getGtk(String psKey){
		return String.valueOf(QqUtils.getGTK(psKey));
	}

	@JSONField(serialize = false)
	public String getGtk2(){
		return QqUtils.getGTK2(sKey);
	}

	@JSONField(serialize = false)
	public String getGtkP(){
		return String.valueOf(QqUtils.getGTK(psKey));
	}

	@JSONField(serialize = false)
	public String getToken(){
		return String.valueOf(QqUtils.getToken(superToken));
	}

	@JSONField(serialize = false)
	public String getToken2(){
		return String.valueOf(QqUtils.getToken2(superToken));
	}
}
