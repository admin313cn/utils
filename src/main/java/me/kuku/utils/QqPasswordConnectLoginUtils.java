package me.kuku.utils;

import lombok.Data;
import me.kuku.pojo.Result;
import me.kuku.pojo.TencentCaptcha;
import me.kuku.pojo.UA;
import okhttp3.Response;

import java.io.IOException;
import java.net.URLEncoder;

public class QqPasswordConnectLoginUtils {

	@Data
	private static class QqVc{
		private Long appId;
		private Integer daId;
		private Long ptAid;
		private String h5sig;
		private String cookie;
		private String ptdRvs;
		private String redirectUrl;
		private String xuiUrl;
		private String time;

		private Boolean needCaptcha;
		private Integer code;
		private String randomStr;
		private String ticket;
		private String sid;

		public QqVc(Long appId, Integer daId, Long ptAid, String h5sig, String cookie, String ptdRvs, String redirectUrl, String xuiUrl) {
			this.appId = appId;
			this.daId = daId;
			this.ptAid = ptAid;
			this.h5sig = h5sig;
			this.cookie = cookie;
			this.ptdRvs = ptdRvs;
			this.redirectUrl = redirectUrl;
			this.xuiUrl = xuiUrl;
		}
	}

	private static QqVc checkVc(Long qq, Long ptAid, String redirectUrl) throws IOException {
		String graphStr = OkHttpUtils.getStr("https://graph.qq.com/oauth2.0/authorize?response_type=code&client_id=" + ptAid +
						"&redirect_uri=" + URLEncoder.encode(redirectUrl, "utf-8") + "&scope=get_user_info",
				OkHttpUtils.addUA(UA.QQ));
		String enUrl = MyUtils.regex("content=\"1;url=", "\"", graphStr);
		String xuiUrl = enUrl.replaceAll("&#61;", "=").replaceAll("&amp;", "&");
		Response xuiResponse = OkHttpUtils.get(xuiUrl, OkHttpUtils.addUA(UA.QQ));
		xuiResponse.close();
		String xuiCookie = OkHttpUtils.getCookie(xuiResponse);
		String appId = MyUtils.regex("appid=", "&", xuiUrl);
		String daId = MyUtils.regex("daid=", "&", xuiUrl);
		String h5sig = MyUtils.regex("h5sig=", "&", xuiUrl);
		String time = MyUtils.regex("time=", "&", xuiUrl);
		Response checkResponse = OkHttpUtils.get("https://xui.ptlogin2.qq.com/ssl/check?pt_tea=2&uin=" + qq + "&appid=" + appId
				+ "&ptlang=2052&regmaster=&pt_uistyle=35&r=0." + MyUtils.randomNum(16),
				OkHttpUtils.addHeaders(xuiCookie, xuiUrl, UA.QQ));
		String checkCookie = OkHttpUtils.getCookie(checkResponse);
		String cookie = xuiCookie + checkCookie;
		String ptdRvs = OkHttpUtils.getCookie(checkCookie, "ptdrvs");
		String ptVfSession = OkHttpUtils.getCookie(checkCookie, "ptvfsession");
		String checkStr = OkHttpUtils.getStr(checkResponse);
		String[] arr = checkStr.substring(checkStr.indexOf("('") + 2, checkStr.lastIndexOf('\'')).split("','");
		int code = Integer.parseInt(arr[0]);
		boolean needCaptcha = code == 1;
		QqVc qqVc = new QqVc(Long.parseLong(appId), Integer.parseInt(daId), ptAid, h5sig, cookie, ptdRvs, redirectUrl, xuiUrl);
		qqVc.time = time;
		qqVc.setCode(code);
		qqVc.setNeedCaptcha(needCaptcha);
		qqVc.setSid(arr[6]);
		if (!needCaptcha){
			qqVc.setRandomStr(arr[1]);
			qqVc.setTicket(ptVfSession);
		}
		return qqVc;
	}

	private static Result<String> login(long qq, String password, QqVc qqVc) throws IOException {
		long idt = System.currentTimeMillis() / 1000 - 5;
		String encryptPassword = QqUtils.encryptPassword(qq, password, qqVc.randomStr);
		String redirectUrl = URLEncoder.encode(URLEncoder.encode(qqVc.redirectUrl, "utf-8"), "utf-8");
		String uri = "https://xui.ptlogin2.qq.com/ssl/pt_open_login?openlogin_data=which%3D%26refer_cgi%3Dauthorize%26response_type%3Dcode%26client_id%3D" +
				qqVc.ptAid + "%26state%3D%26display%3D%26openapi%3D1010_1011%26switch%3D0%26src%3D1%26sdkv%3Dv1.0%26sdkp%3Dpcweb%26tid%3D" +
				idt + "%26pf%3D%26need_pay%3D0%26browser%3D0%26browser_error%3D%26serial%3D%26token_key%3D%26redirect_uri%3D" +
				redirectUrl + "%26sign%3D%26time%3D" + qqVc.time + "%26status_version%3D%26status_os%3D%26status_machine%3D%26page_type%3D1%26has_auth%3D1%26update_auth%3D1%26auth_time%3D" +
				System.currentTimeMillis() + "%26loginfrom%3D%26h5sig%3D" + qqVc.h5sig + "%26loginty%3D3%26&ptdrvs=" + qqVc.ptdRvs +
				"&pt_vcode_v1=" + qqVc.code + "&pt_verifysession_v1=" + qqVc.ticket + "&verifycode=" + qqVc.randomStr +
				"&u=" + qq + "&p=" + encryptPassword + "&pt_randsalt=2&ptlang=2052&low_login_enable=0&u1=https%3A%2F%2Fconnect.qq.com&from_ui=1&fp=loginerroralert&device=2&aid=" +
				qqVc.appId + "&daid=" + qqVc.daId + "&pt_3rd_aid=" + qqVc.ptAid + "&ptredirect=1&h=1&g=1&pt_uistyle=35&regmaster=&";
		Response response = OkHttpUtils.get(uri, OkHttpUtils.addHeaders(qqVc.getCookie() + "idt=" + idt + "; ", qqVc.getXuiUrl(), UA.QQ));
		String str = OkHttpUtils.getStr(response);
		Result<String> result = QqUtils.getResultUrl(str);
		switch (result.getCode()){
			case 200:
				return result;
			case 502: return Result.failure("登录失败，请稍后再试！", null);
			default: return Result.failure(result.getMessage(), null);
		}
	}

	public static Result<String> login(Long qq, String password, Long ptAid, String url) throws IOException {
		QqVc qqVc = checkVc(qq, ptAid, url);
		if (qqVc.needCaptcha){
			Result<TencentCaptcha> result = TenCentCaptchaUtils.identify(qqVc.appId, qqVc.sid, qqVc.xuiUrl);
			if (result.getCode() == 200) {
				TencentCaptcha captcha = result.getData();
				qqVc.setTicket(captcha.getTicket());
				qqVc.setRandomStr(captcha.getRandomStr());
			}
			else return Result.failure(result.getMessage());
		}
		return login(qq, password, qqVc);
	}




}
