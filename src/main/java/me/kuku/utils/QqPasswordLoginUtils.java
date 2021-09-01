package me.kuku.utils;

import lombok.Data;
import me.kuku.pojo.*;
import okhttp3.Response;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.Map;

@SuppressWarnings("DuplicatedCode")
public class QqPasswordLoginUtils {

	@Data
	private static class QqVc{
		private Long appId;
		private Integer daId;
		private String cookie;
		private String loginSig;
		private String ptdRvs;
		private String redirectUrl;
		private String enRedirectUrl;
		private String xuiUrl;

		private Boolean needCaptcha;
		private Integer code;
		private String randomStr;
		private String ticket;
		private String sid;

		public QqVc(Long appId, Integer daId, String cookie, String loginSig, String ptdRvs, String redirectUrl, String enRedirectUrl, String xuiUrl){
			this.appId = appId;
			this.daId = daId;
			this.cookie = cookie;
			this.loginSig = loginSig;
			this.ptdRvs = ptdRvs;
			this.redirectUrl = redirectUrl;
			this.enRedirectUrl = enRedirectUrl;
			this.xuiUrl = xuiUrl;
		}
	}

	private static QqVc checkVc(QqApp qqApp, long qq, String redirectUrl) throws IOException {
		String enRedirectUrl = URLEncoder.encode(redirectUrl, "utf-8");
		String xuiUrl = "https://xui.ptlogin2.qq.com/cgi-bin/xlogin?appid=" +
				qqApp.getAppId() + "&style=20&s_url=" + enRedirectUrl + "&maskOpacity=60&daid=" + qqApp.getDaId() + "&target=self";
		Response xuiResponse = OkHttpUtils.get(xuiUrl);
		xuiResponse.close();
		String xuiCookie = OkHttpUtils.getCookie(xuiResponse);
		String loginSig = OkHttpUtils.getCookie(xuiCookie, "pt_login_sig");
		Response checkResponse = OkHttpUtils.get("https://ssl.ptlogin2.qq.com/check?regmaster=&pt_tea=2&pt_vcode=1&uin=" + qq + "&appid=" +
				qqApp.getAppId() + "&js_ver=21082415&js_type=1&login_sig=" + loginSig + "&u1=" + enRedirectUrl + "&r=0." +
				MyUtils.randomNum(15) + "&pt_uistyle=40", OkHttpUtils.addHeaders(xuiCookie, "https://xui.ptlogin2.qq.com/",
				UA.PC));
		String checkCookie = OkHttpUtils.getCookie(checkResponse);
		String cookie = xuiCookie + checkCookie;
		String ptdRvs = OkHttpUtils.getCookie(checkCookie, "ptdrvs");
		String ptVfSession = OkHttpUtils.getCookie(checkCookie, "ptvfsession");
		String checkStr = OkHttpUtils.getStr(checkResponse);
		String[] arr = checkStr.substring(checkStr.indexOf("('") + 2, checkStr.lastIndexOf('\'')).split("','");
		int code = Integer.parseInt(arr[0]);
		boolean needCaptcha = code == 1;
		QqVc qqVc = new QqVc(qqApp.getAppId(), qqApp.getDaId(), cookie, loginSig, ptdRvs, redirectUrl, enRedirectUrl, xuiUrl);
		qqVc.setCode(code);
		qqVc.setNeedCaptcha(needCaptcha);
		qqVc.setSid(arr[6]);
		if (!needCaptcha){
			qqVc.setRandomStr(arr[1]);
			qqVc.setTicket(ptVfSession);
		}
		return qqVc;
	}

	private static Result<QqLoginPojo> login(long qq, String password, QqVc qqVc) throws IOException {
		String encryptPassword = QqUtils.encryptPassword(qq, password, qqVc.randomStr);
		String url = "https://ssl.ptlogin2.qq.com/login?u=" + qq +
				"&verifycode=" + qqVc.randomStr + "&pt_vcode_v1=" + qqVc.code + "&pt_verifysession_v1=" +
				qqVc.ticket + "&p=" + encryptPassword + "&pt_randsalt=2&u1=" + qqVc.enRedirectUrl + "&ptredirect=0&h=1&t=1&g=1&from_ui=1&ptlang=2052&action=9-35-" +
				System.currentTimeMillis() + "&js_ver=21082415&js_type=1&login_sig=" + qqVc.loginSig + "&pt_uistyle=40&aid=" +
				qqVc.appId + "&daid=" + qqVc.daId + "&ptdrvs=" + qqVc.ptdRvs + "&sid=" + qqVc.sid + "&";
		Response response = OkHttpUtils.get(url,
				OkHttpUtils.addHeaders(qqVc.cookie, "https://xui.ptlogin2.qq.com/", UA.PC));
		String resultCookie = OkHttpUtils.getCookie(response);
		String str = OkHttpUtils.getStr(response);
		Result<String> result = QqUtils.getResultUrl(str);
		switch (result.getCode()){
			case 200:
				QqLoginPojo qqLoginPojo = new QqLoginPojo();
				qqLoginPojo.setQq(qq);
				qqLoginPojo.setSKey(OkHttpUtils.getCookie(resultCookie, "skey"));
				qqLoginPojo.setSuperKey(OkHttpUtils.getCookie(resultCookie, "superkey"));
				qqLoginPojo.setSuperToken(OkHttpUtils.getCookie(resultCookie, "supertoken"));
				Map<String, String> otherKeys = QqUtils.getKey(result.getData());
				qqLoginPojo.setPsKey(otherKeys.get("p_skey"));
				qqLoginPojo.setPt4Token(otherKeys.get("pt4_token"));
				qqLoginPojo.setPtOauthToken(otherKeys.get("pt_oauth_token"));
				qqLoginPojo.setPtLoginType(otherKeys.get("pt_login_type"));
				return Result.success(qqLoginPojo);
			case 502: return Result.failure("登录失败，请稍后再试！", null);
			default: return Result.failure(result.getMessage(), null);
		}
	}

	public static Result<QqLoginPojo> login(Long qq, String password, QqApp qqApp, String url) throws IOException {
		QqVc qqVc = checkVc(qqApp, qq, url);
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

	public static Result<QqLoginPojo> login(Long qq, String password, Long appId, Integer daId, String url) throws IOException {
		return login(qq, password, new QqApp(appId, daId), url);
	}

}
