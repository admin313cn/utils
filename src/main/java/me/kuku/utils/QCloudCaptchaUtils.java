package me.kuku.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import me.kuku.pojo.Result;
import me.kuku.pojo.TencentCaptcha;
import me.kuku.pojo.UA;
import okhttp3.Response;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class QCloudCaptchaUtils {
	private final static String ua = "TW96aWxsYS81LjAgKExpbnV4OyBBbmRyb2lkIDExOyBSTVgzMDMxIEJ1aWxkL1JQMUEuMjAwNzIwLjAxMTsgd3YpIEFwcGxlV2ViS2l0LzUzNy4zNiAoS0hUTUwsIGxpa2UgR2Vja28pIFZlcnNpb24vNC4wIENocm9tZS85Mi4wLjQ1MTUuMTMxIE1vYmlsZSBTYWZhcmkvNTM3LjM2";

	private static String getCaptchaPictureUrl(String appId, String sig, String sess, String sid, String webSig, String vSig, String rnd, String loadTime1, String loadTime2, String start, int index){
		int subSid = 3;
		return String.format("https://captcha.guard.qcloud.com/cap_union_new_getcapbysig?asig=%s&aid=%s&captype=&protocol=https&clientype=1&disturblevel=&apptype=&noheader=0&color=&showtype=popup&fb=1&theme=&lang=2052&ua=%s&sess=%s&fwidth=0&sid=%s&subsid=%s&uid=&cap_cd=&rnd=%s&forcestyle=undefined&wxLang=&TCapIframeLoadTime=%s&prehandleLoadTime=%s&createIframeStart=%s&rand=0.0%s&websig=%s&vsig=%s&img_index=%s",
				sig, appId, ua, sess, sid, subSid, rnd, loadTime2, loadTime1, start, MyUtils.randomNum(15) , webSig, vSig, index);
	}

	private static Map<String, String> getCollect(int width) throws IOException {
		String js = OkHttpUtils.getStr("https://dj.captcha.qq.com/tdc.js?v=1.6.2");
		String base64Str = Base64.getEncoder().encodeToString(js.getBytes());
		Map<String, String> map = new HashMap<>();
		map.put("script", base64Str);
		map.put("width", String.valueOf(width));
		JSONObject jsonObject = OkHttpUtils.postJson("https://api.kukuqaq.com/exec/collect", map);
		String collectData = URLDecoder.decode(jsonObject.getString("collectData"), "utf-8");
		String eks = jsonObject.getString("eks");
		String length = String.valueOf(collectData.length());
		Map<String, String> result = new HashMap<>();
		result.put("collectData", collectData);
		result.put("eks", eks);
		result.put("length", length);
		return result;
	}

	private static int getWidth(String imageAUrl, String imageBUrl) throws IOException {
		BufferedImage imageA = ImageIO.read(new URL(imageAUrl));
		BufferedImage imageB = ImageIO.read(new URL(imageBUrl));
		int imgWidth = imageA.getWidth();
		int imgHeight = imageA.getHeight();
		int t = 0, r = 0;
		for (int i = 0; i < imgHeight - 20; i++){
			for (int j = 0; j < imgWidth - 20; j++){
				int rgbA = imageA.getRGB(j, i);
				int rgbB = imageB.getRGB(j, i);
				if (Math.abs(rgbA - rgbB) > 1800000){
					t++;
					r += j;
				}
			}
		}
		return Math.round(Float.parseFloat(String.valueOf(r / t))) -55;
	}

	private static Map<String, String> getCaptcha(String appId, String sig) throws IOException {
		JSONObject jsonObject = OkHttpUtils.getJsonp(String.format("https://captcha.guard.qcloud.com/cap_union_prehandle?asig=%s&aid=%s&captype=&protocol=https&clientype=1&disturblevel=&apptype=&noheader=0&color=&showtype=popup&fb=1&theme=&lang=2052&ua=%s&cap_cd=&uid=&callback=_aq_813860&sess=&subsid=1",
				sig, appId, ua));
		String randNum = MyUtils.randomNum(6);
		String loadTime1 = "19";
		String loadTime2 = "129";
		String start = String.valueOf(System.currentTimeMillis());
		String url = String.format("https://captcha.guard.qcloud.com/cap_union_new_show?asig=%s&aid=%s&captype=&protocol=https&clientype=1&disturblevel=&apptype=&noheader=0&color=&showtype=popup&fb=1&theme=&lang=2052&ua=%s&sess=%s&fwidth=0&sid=%s&subsid=2&uid=&cap_cd=&rnd=%s&forcestyle=undefined&wxLang=&TCapIframeLoadTime=%s&prehandleLoadTime=%s&createIframeStart=%s",
				sig, appId, ua, jsonObject.getString("sess"), jsonObject.getString("sid"), randNum, loadTime1, loadTime2, start);
		String html = OkHttpUtils.getStr(url);
		String height = MyUtils.regex("st=Number\\(\"", "\"\\)", html);
		String collectName = MyUtils.regex("\"\\+a\\+\"&", "=\"\\+_\\+\"", html);
		String sess = jsonObject.getString("sess");
		String webSig = MyUtils.regex("&websig=", "\"", html);
		String vSig = MyUtils.regex("Q=\"", "\",", html);
		String imageAUrl = getCaptchaPictureUrl(appId, sig, sess, jsonObject.getString("sid"), webSig, vSig, randNum, loadTime1, loadTime2, start, 1);
		String imageBUrl = getCaptchaPictureUrl(appId, sig, sess, jsonObject.getString("sid"), webSig, vSig, randNum, loadTime1, loadTime2, start, 0);
		int width = getWidth(imageAUrl, imageBUrl);
		String ans = width + "," + height + ";";
		Map<String, String> collect = getCollect(width);
		Map<String, String> map = new HashMap<>();
		map.put("sess", sess);
		map.put("sid", jsonObject.getString("sid"));
		map.put("sig", sig);
		map.put("ans", ans);
		map.put("collectName", collectName);
		map.put("cdata", "0");
		map.put("width", String.valueOf(width));
		map.put("url", url);
		map.put("webSig", webSig);
		map.put("vSig", vSig);
		map.put("loadTime1", loadTime1);
		map.put("loadTime2", loadTime2);
		map.put("start", start);
		map.put("randNum", randNum);
		map.putAll(collect);
		return map;
	}

	private static Result<TencentCaptcha> identifyCaptcha(String appId, Map<String, String> map) throws IOException {
		Response response = OkHttpUtils.get("https://ssl.captcha.qq.com/dfpReg?0=Mozilla%2F5.0%20(Linux%3B%20Android%2011%3B%20RMX3031%20Build%2FRP1A.200720.011%3B%20wv)%20AppleWebKit%2F537.36%20(KHTML%2C%20like%20Gecko)%20Version%2F4.0%20Chrome%2F92.0.4515.131%20Mobile%20Safari%2F537.36&1=zh-CN&2=2.2&3=2.2&4=24&5=8&6=-480&7=1&8=1&9=1&10=u&11=function&12=u&13=Linux%20aarch64&14=0&15=e466827d3971a555235e032f6e6f19d2&16=56e0da52b1f1919311eedd317fddc640&17=a1f937b6ee969f22e6122bdb5cb48bde&18=10x1x10x1&19=511ad6d3a33f320317431bb33c1a656e&20=8003602480036024&21=3%3B&22=1%3B1%3B1%3B1%3B1%3B1%3B1%3B0%3B1%3Bobject0UTF-8&23=0&24=5%3B1&25=2ef8be5d2413046bf04e52c22e8e3b90&26=48000_2_1_0_2_explicit_speakers&27=c8205b36aba2b1f3b581d8170984e918&28=Mali-G77MC9&29=cf83512421699aed3e43c7b85781492d&30=a23a38527a38dcea2f63d5d078443f78&31=0&32=0&33=0&34=0&35=0&36=0&37=0&38=0&39=0&40=0&41=0&42=0&43=0&44=0&45=0&46=0&47=0&48=0&49=0&50=0&fesig=8623683915260939468&ut=816&appid=0&refer=https%3A%2F%2Fcaptcha.guard.qcloud.com%2Fcap_union_new_show&domain=captcha.guard.qcloud.com&fph=11001078B34667D88864BDE926C5B244CA6C4C11A944E5413BF335F56EBF592C1A04C6718F323FA2FD1C7F4098A3FD553788&fpv=0.0.15&ptcz=&callback=_fp_020387",
				OkHttpUtils.addReferer(map.get("url")));
		String cookie = OkHttpUtils.getCookie(response);
		String str = OkHttpUtils.getStr(response);
		String fpSig = JSON.parseObject(MyUtils.regex("\\{.*\\}", str)).getString("fpsig");
		Map<String, String> paramsMap = new HashMap<>();
		paramsMap.put("asig", map.get("sig"));
		paramsMap.put("aid", appId);
		paramsMap.put("captype", "");
		paramsMap.put("protocol", "https");
		paramsMap.put("clientype", "1");
		paramsMap.put("disturblevel", "");
		paramsMap.put("apptype", "");
		paramsMap.put("noheader", "0");
		paramsMap.put("color", "");
		paramsMap.put("showtype", "popup");
		paramsMap.put("fb", "1");
		paramsMap.put("theme", "");
		paramsMap.put("lang", "2052");
		paramsMap.put("ua", ua);
		paramsMap.put("sess", map.get("sess"));
		paramsMap.put("fwidth", "0");
		paramsMap.put("sid", map.get("sid"));
		paramsMap.put("subsid", "6");
		paramsMap.put("uid", "");
		paramsMap.put("cap_cd", "");
		paramsMap.put("rnd", map.get("randNum"));
		paramsMap.put("forcestyle", "undefined");
		paramsMap.put("wxLang", "");
		paramsMap.put("TCapIframeLoadTime", map.get("loadTime2"));
		paramsMap.put("prehandleLoadTime", map.get("loadTime1"));
		paramsMap.put("createIframeStart", map.get("start"));
		paramsMap.put("rand", "0." + MyUtils.randomNum(16));
		paramsMap.put("build", "");
		paramsMap.put("subcapclass", "10");
		paramsMap.put("vsig", map.get("vSig"));
		paramsMap.put("ans", map.get("ans"));
		paramsMap.put(map.get("collectName"), map.get("collectData"));
		paramsMap.put("websig", map.get("webSig"));
		paramsMap.put("cdata", "91");
		paramsMap.put("fpinfo", "fpsig=" + fpSig);
		paramsMap.put("eks", map.get("eks"));
		paramsMap.put("tlg", "1");
		paramsMap.put("vlg", "0_0_0");
		paramsMap.put("vmtime", "_");
		paramsMap.put("vmData", "");
		JSONObject jsonObject = OkHttpUtils.postJson("https://captcha.guard.qcloud.com/cap_union_new_verify", paramsMap,
				OkHttpUtils.addHeaders(cookie, map.get("url"), UA.PIXEL));
		if (jsonObject.getInteger("errorCode") == 0){
			TencentCaptcha tencentCaptcha = new TencentCaptcha(jsonObject.getString("ticket"), jsonObject.getString("randstr"));
			return Result.success(tencentCaptcha);
		}else return Result.failure(400, "验证码识别失败，请稍后重试！！");
	}

	public static Result<TencentCaptcha> identify(String appId, String sig) throws IOException {
		Map<String, String> map = getCaptcha(appId, sig);
		return identifyCaptcha(appId, map);
	}
}
