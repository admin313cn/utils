package me.kuku.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import me.kuku.pojo.HiToKoTo;
import me.kuku.pojo.UA;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MyUtils {
	private static final Map<String, Pattern> patternMap = new HashMap<>();
	private static final DecimalFormat df = new DecimalFormat("#.00");

	public static String regex(String regex, String text){
		Pattern pattern;
		if (patternMap.containsKey(regex))
			pattern = patternMap.get(regex);
		else {
			pattern = Pattern.compile(regex);
			patternMap.put(regex, pattern);
		}
		Matcher matcher = pattern.matcher(text);
		if (matcher.find()){
			return matcher.group();
		}
		return null;
	}

	public static String regex(String first, String last, String text){
		String regex = String.format("(?<=%s).*?(?=%s)", first, last);
		return regex(regex, text);
	}

	private static String random(String str, int length){
		StringBuilder result = new StringBuilder();
		for (int i = 0; i < length; i++){
			int at = (int) (Math.random() * str.length());
			result.append(str.charAt(at));
		}
		return result.toString();
	}

	public static String randomStr(int length){
		return random("1234567890abcdefghijklmnopqrstuvwxyz", length);
	}

	public static String random(int length){
		return random("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz123456789", length);
	}

	public static String randomNum(int length){
		return random("1234567890", length);
	}

	public static Long randomLong(long min, long max){
		return ((long) (Math.random() * max)) % (max - min + 1) + min;
	}

	public static int randomInt(int min, int max){
		return ((int) (Math.random() * max)) % (max - min + 1) + min;
	}

	public static String randomStrLetter(int length){
		return random("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ", length);
	}

	public static String removeLastLine(StringBuilder sb){
		return sb.deleteCharAt(sb.length() - 1).toString();
	}

	public static String parseSize(long size){
		// k m g t
		double d = size;
		double result = 0;
		String suffix = "";
		if (size > 1024L * 1024 * 1024 * 1024) {
			result = d / 1024 / 1024 / 1024 / 1024;
			suffix = "TB";
		} else if (size > 1024L * 1024 * 1024) {
			result = d / 1024 / 1024 / 1024;
			suffix = "GB";
		} else if (size > 1024L * 1024) {
			result = d / 1024 / 1024;
			suffix = "MB";
		} else if (size > 1024L) {
			result = d / 1024;
			suffix = "KB";
		} else {
			result = d;
			suffix = "B";
		}
		if (result == 0) return result + suffix;
		return df.format(result) + suffix;
	}

	public static byte[] creatQr(String content) throws IOException {
		return OkHttpUtils.getBytes("https://www.zhihu.com/qrcode?url=" + URLEncoder.encode(content, "utf-8"));
	}

	public static HiToKoTo hiToKoTo() throws IOException {
		JSONObject jsonObject = OkHttpUtils.getJson("https://v1.hitokoto.cn/");
		return JSON.parseObject(jsonObject.toString(), HiToKoTo.class);
	}

	public static String pasteUbuntu(String poster, String syntax, String content) {
		Map<String, String> map = new HashMap<>();
		map.put("poster", poster);
		map.put("syntax", syntax);
		map.put("content", content);
		try {
			JSONObject jsonObject = OkHttpUtils.postJson("https://api.kukuqaq.com/tool/paste", map, OkHttpUtils.addUA(UA.PC));
			return jsonObject.getJSONObject("data").getString("data");
		} catch (IOException e) {
			return "生成失败！！";
		}
	}

	public static String exPasteUbuntu(Exception e) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		return pasteUbuntu("exception", "java", sw.toString());
	}


}
