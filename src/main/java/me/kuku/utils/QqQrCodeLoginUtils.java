package me.kuku.utils;

import me.kuku.pojo.QqLoginPojo;
import me.kuku.pojo.QqLoginQrcode;
import me.kuku.pojo.Result;
import me.kuku.pojo.UA;
import okhttp3.Response;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class QqQrCodeLoginUtils {

	public static QqLoginQrcode getQrCode(long appId, int daId, long ptAid) throws IOException {
		Response response = OkHttpUtils.get(String.format("https://ssl.ptlogin2.qq.com/ptqrshow?appid=%s&e=2&l=M&s=3&d=72&v=4&t=0.%s&daid=%s&pt_3rd_aid=0" + ptAid,
				appId, MyUtils.randomStr(17), daId));
		byte[] bytes = OkHttpUtils.getBytes(response);
		String cookie = OkHttpUtils.getCookie(response);
		String sig = OkHttpUtils.getCookie(cookie, "qrsig");
		return new QqLoginQrcode(bytes, sig);
	}

	public static QqLoginQrcode getQrCode() throws IOException {
		return getQrCode(549000912L, 5, 0);
	}

	public static Result<QqLoginPojo> checkQrCode(long appId, int daId, long ptAid, String url, String sig) throws IOException {
		Response response = OkHttpUtils.get(String.format("https://ssl.ptlogin2.qq.com/ptqrlogin?u1=%s&ptqrtoken=%s&ptredirect=0&h=1&t=1&g=1&from_ui=1&ptlang=2052&action=0-0-1591074900575&js_ver=20032614&js_type=1&login_sig=&pt_uistyle=40&aid=%s&daid=%s&pt_3rd_aid=%s&",
				URLEncoder.encode(url, "utf-8"), getPtGrToken(sig), appId, daId, ptAid), OkHttpUtils.addCookie("qrsig=" + sig));
		String str = OkHttpUtils.getStr(response);
		switch (Integer.parseInt(MyUtils.regex("'", "','", str))){
			case 0:
				String cookie = OkHttpUtils.getCookie(response);
				Map<String, String> cookieMap = OkHttpUtils.getCookie(cookie, "skey", "superkey", "supertoken");
				String qqq = OkHttpUtils.getCookie(cookie, "pt2gguin");
				String qqStr = MyUtils.regex("[1-9][0-9]{4,}", qqq);
				long qq;
				if (qqStr == null) qq = 0L;
				else qq = Long.parseLong(qqStr);
				Result<String> result = QqUtils.getResultUrl(str);
				Map<String, String> map = QqUtils.getKey(result.getData());
				QqLoginPojo qqLoginPojo = new QqLoginPojo(qq, cookieMap.get("skey"), map.get("p_skey"),
						cookieMap.get("superkey"), cookieMap.get("supertoken"), map.get("pt4_token"));
				return Result.success(qqLoginPojo);
			case 66:
			case 67:
				return Result.failure(0, "未失效或者验证中！");
			default: return Result.failure(MyUtils.regex("','','0','", "', ''", str), null);
		}
	}

	@SuppressWarnings("ConstantConditions")
	public static Result<String> authorize(QqLoginPojo qqLoginPojo, long clientId, String state, String redirectUri) throws IOException {
		Map<String, String> map = new HashMap<>();
		map.put("response_type", "code");
		map.put("client_id", String.valueOf(clientId));
		map.put("redirect_uri", redirectUri);
		map.put("scope", "all");
		map.put("state", state);
		map.put("switch", "");
		map.put("from_ptlogin", "1");
		map.put("src", "1");
		map.put("update_auth", "1");
		map.put("openapi", "80901010");
		map.put("g_tk", qqLoginPojo.getGtkP());
		map.put("auth_time", String.valueOf(System.currentTimeMillis()));
		String ui = UUID.randomUUID().toString();
		map.put("ui", ui);
		Response response = OkHttpUtils.post("https://graph.qq.com/oauth2.0/authorize",
				map, OkHttpUtils.addHeaders(qqLoginPojo.getCookieWithPs() + "ui=" + ui + "; ", "https://graph.qq.com/oauth2.0/show?which=Login&display=pc&client_id=" + clientId + "&response_type=code&scope=all&redirect_uri=" + URLEncoder.encode(redirectUri, "utf-8"),
						UA.PC));
		response.close();
		String url = response.header("location");
		if (url.contains("https://graph.qq.com/oauth2.0/show?which=error")) return Result.failure("失败，请重试！");
		else return Result.success(url);
	}

	public static Result<QqLoginPojo> checkQrCode(String sig) throws IOException {
		return checkQrCode(549000912L, 5, 0L, "https://qzs.qzone.qq.com/qzone/v5/loginsucc.html?para=izone", sig);
	}

	private static int getPtGrToken(String sig) {
		int e = 0;
		for (int i = 0, n = sig.length(); n > i; ++i)
			e += (e << 5) + (int) sig.charAt(i);
		return e & 2147483647;
	}
}
