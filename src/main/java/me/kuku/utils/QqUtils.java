package me.kuku.utils;

import me.kuku.pojo.Result;
import okhttp3.Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QqUtils {
	public static String getGTK2(String sKey) {
		long salt = 5381;
		String md5key = "tencentQQVIP123443safde&!%^%1282";
		List<Long> hash = new ArrayList<>();
		hash.add(salt << 5);
		int len = sKey.length();
		for (int i = 0; i < len; i++){
			String ASCIICode = Integer.toHexString(sKey.charAt(i));
			long code = Integer.valueOf(ASCIICode, 16);
			hash.add((salt << 5) + code);
			salt = code;
		}
		StringBuilder sb = new StringBuilder();
		hash.forEach(sb::append);
		String md5str = sb + md5key;
		md5str = MD5Utils.toMD5(md5str);
		return md5str;
	}

	public static long getGTK(String psKey){
		int len = psKey.length();
		long hash = 5381L;
		for (int i = 0; i < len; i++){
			hash += (hash << 5 & 2147483647) + (int) psKey.charAt(i) & 2147483647;
			hash &= 2147483647;
		}
		return hash & 2147483647;
	}

	public static Long getToken(String token){
		int len = token.length();
		long hash = 0L;
		for (int i = 0; i < len; i++){
			hash = (hash * 33 + (int) token.charAt(i)) % 4294967296L;
		}
		return hash;
	}

	public static Long getToken2(String token){
		int len = token.length();
		long hash = 0L;
		for (int i = 0; i < len; i++){
			hash += (hash << 5) + (((int) token.charAt(i)) & 2147483647);
			hash = hash & 2147483647;
		}
		return hash & 2147483647;
	}

	public static Result<String> getResultUrl(String str){
		String ss = MyUtils.regex("'", "','", str);
		String msg = null;
		if (ss == null) {
			msg = MyUtils.regex(",'0','", "', ' '", str);
			if (msg == null) msg = "其他错误";
		}
		if (msg == null) {
			int num = Integer.parseInt(ss);
			switch (num) {
				case 4:
					msg = "验证码错误，登录失败！！";
					break;
				case 3:
					msg = "密码错误，登录失败！！";
					break;
				case 19:
					msg = "您的QQ号已被冻结，登录失败！";
					break;
				case 10009:
					return Result.failure(10009, "您的QQ号登录需要验证短信，请输入短信验证码！！");
				case 0:
				case 2: {
					String url = MyUtils.regex(",'0','", "','", str);
					if (url == null) url = MyUtils.regex("','", "'", str);
					if (url != null) return Result.success(url);
					else msg = "";
					break;
				}
				case 1:
				case -1:
				case 7:
					msg = "superKey已失效，请更新QQ！";
					break;
				case 23003:
					msg = "当前上网环境异常，请更换网络环境或在常用设备上登录或稍后再试。请尝试扫码登录。";
					break;
				default:
					msg = str;
			}
		}
		if (msg.contains("superKey")) return Result.failure(502, msg);
		return Result.failure(500, msg);
	}

	public static Map<String, String> getKey(String pt, String qq, String domain, String suffixUrl) throws IOException {
		return getKey(String.format("https://%s/check_sig?uin=%s&ptsigx=%s%s", domain, qq, pt, suffixUrl));
	}

	public static Result<String> getPtToken(String str){
		Result<String> result = getResultUrl(str);
		if (result.getCode() == 200){
			String url = result.getData();
			String token = MyUtils.regex("ptsigx=", "&", url);
			return Result.success(token);
		}else return result;
	}

	public static Map<String, String> getKey(String url) throws IOException {
		Response response = OkHttpUtils.get(url);
		response.close();
		String cookie = OkHttpUtils.getCookie(response);
		Map<String, String> map = new HashMap<>();
		map.put("p_skey", OkHttpUtils.getCookie(cookie, "p_skey"));
		map.put("pt4_token", OkHttpUtils.getCookie(cookie, "pt4_token"));
		map.put("pt_oauth_token", OkHttpUtils.getCookie(cookie, "pt_oauth_token"));
		map.put("pt_login_type", OkHttpUtils.getCookie(cookie, "pt_login_type"));
		return map;
	}
}
