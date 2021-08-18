package me.kuku.utils;

import com.alibaba.fastjson.JSONObject;
import me.kuku.pojo.Result;
import me.kuku.pojo.TencentCaptcha;
import me.kuku.pojo.UA;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class TenCentCaptchaUtils {

    private final static String en_ua = "TW96aWxsYS81LjAgKExpbnV4OyBBbmRyb2lkIDEwOyBWMTkxNEEgQnVpbGQvUVAxQS4xOTA3MTEuMDIwOyB3dikgQXBwbGVXZWJLaXQvNTM3LjM2IChLSFRNTCwgbGlrZSBHZWNrbykgVmVyc2lvbi80LjAgQ2hyb21lLzY2LjAuMzM1OS4xMjYgTVFRQnJvd3Nlci82LjIgVEJTLzA0NTEzMiBNb2JpbGUgU2FmYXJpLzUzNy4zNiBWMV9BTkRfU1FfOC4zLjBfMTM2Ml9ZWUJfRCBRUS84LjMuMC40NDgwIE5ldFR5cGUvNEcgV2ViUC8wLjMuMCBQaXhlbC8xMDgwIFN0YXR1c0JhckhlaWdodC84NSBTaW1wbGVVSVN3aXRjaC8wIFFRVGhlbWUvMTAwMA%3D%3D";

    private static String getCaptchaPictureUrl(Long appId, String imageId, String sess, String sid, int index){
        return "https://t.captcha.qq.com/hycdn?index=" + index + "&image=" + imageId + "?aid=" + appId + "&sess=" + sess + "&sid=" + sid + "&img_index=" + index + "&subsid=5";
    }

    private static Map<String, String> getCollect(int width, String suffixUrl, String showUrl) throws IOException {
        String js = OkHttpUtils.getStr("https://t.captcha.qq.com/" + suffixUrl,
                OkHttpUtils.addHeaders("", showUrl, UA.QQ));
        String base64Str = Base64.getEncoder().encodeToString(js.getBytes());
        Map<String, String> map = new HashMap<>();
        map.put("script", base64Str);
        map.put("width", String.valueOf(width));
        JSONObject jsonObject = OkHttpUtils.postJson("https://apicf.kuku.me/tool/collectByWidth", map);
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

    private static Map<String, String> getCaptcha(Long appId, String sid, String refererUrl) throws IOException {
        JSONObject jsonObject = OkHttpUtils.getJsonp("https://t.captcha.qq.com/cap_union_prehandle?aid=" + appId
                        + "&protocol=https&accver=1&showtype=popup&ua=" + en_ua + "&noheader=0&fb=1&enableDarkMode=0&sid=" + sid
                        + "&grayscale=1&clientype=1&cap_cd=&uid=&wxLang=&lang=zh&entry_url=" + refererUrl + "&js=%2Ftcaptcha-frame.a75be429.js&subsid=3&callback=_aq_587&sess=",
                OkHttpUtils.addHeaders("", "https://xui.ptlogin2.qq.com/", UA.QQ));
        String sess = jsonObject.getString("sess");
        String createIframeStart = String.valueOf(System.currentTimeMillis());
        String showUrl = "https://t.captcha.qq.com/cap_union_new_show?aid=" + appId + "&protocol=https&accver=1&showtype=popup&ua=" + en_ua
                + "&noheader=0&fb=1&enableDarkMode=0&sid=" + sid + "&grayscale=1&clientype=1&sess=" + sess
                + "&fwidth=0&wxLang=&tcScale=1&uid=&cap_cd=&rnd=" + MyUtils.randomNum(6) + "&prehandleLoadTime=41&createIframeStart=" + createIframeStart + "&subsid=4";
        String html = OkHttpUtils.getStr(showUrl,
                OkHttpUtils.addHeaders("", "https://xui.ptlogin2.qq.com/", UA.QQ));
        String height = MyUtils.regex("spt:\"", "\"", html);
        sess = MyUtils.regex("sess:\"", "\"", html);
        String collectName = MyUtils.regex("collectdata:\"", "\"", html);
        String imageId = MyUtils.regex("image=", "\"", html);
        String suffixUrl = MyUtils.regex("\"tdc\",\"/", "\"", html);
        String pow = MyUtils.regex("prefix:\"", "\"", html) + "808";
        String nonce = MyUtils.regex("nonce:\"", "\"", html);
        String imageAUrl = getCaptchaPictureUrl(appId, imageId, sess, sid, 1);
        String imageBUrl = getCaptchaPictureUrl(appId, imageId, sess, sid, 0);
        int width = getWidth(imageAUrl, imageBUrl);
        String ans = width + "," + height + ";";
        Map<String, String> collect = getCollect(width, suffixUrl, showUrl);
        Map<String, String> map = new HashMap<>();
        map.put("sess", sess);
        map.put("sid", sid);
        map.put("ans", ans);
        map.put("collectName", collectName);
        map.put("cdata", "0");
        map.put("width", String.valueOf(width));
        map.put("pow", pow);
        map.put("nonce", nonce);
        map.put("showUrl", showUrl);
        map.put("createIframeStart",createIframeStart);
        map.putAll(collect);
        return map;
    }

    private static Result<TencentCaptcha> identifyCaptcha(Long appId, String sid, Map<String, String> map) throws IOException {
        Map<String, String> paramsMap = new HashMap<>();
        paramsMap.put("aid", String.valueOf(appId));
        paramsMap.put("protocol", "https");
        paramsMap.put("accver", "1");
        paramsMap.put("showtype", "popup");
        paramsMap.put("ua", en_ua);
        paramsMap.put("noheader", "0");
        paramsMap.put("fb", "1");
        paramsMap.put("enableDarkMode", "0");
        paramsMap.put("sid", sid);
        paramsMap.put("grayscale", "1");
        paramsMap.put("clientype", "1");
        paramsMap.put("sess", map.get("sess"));
        paramsMap.put("fwidth", "0");
        paramsMap.put("wxLang", "0");
        paramsMap.put("tcScale", "1");
        paramsMap.put("uid", "");
        paramsMap.put("cap_cd", "");
        paramsMap.put("rnd", MyUtils.randomNum(6));
        paramsMap.put("prehandleLoadTime", "25");
        paramsMap.put("createIframeStart", map.get("createIframeStart"));
        paramsMap.put("subsid", "2");
        paramsMap.put("cdata", "0");
        paramsMap.put("ans", map.get("ans"));
        paramsMap.put("vsig", "");
        paramsMap.put("websig", "");
        paramsMap.put("subcapclass", "");
        paramsMap.put("pow_answer", map.get("pow"));
        paramsMap.put("pow_calc_time", "17");
        paramsMap.put(map.get("collectName"), map.get("collectData"));
        paramsMap.put("tlg", map.get("length"));
        paramsMap.put("fpinfo", "");
        paramsMap.put("eks", map.get("eks"));
        paramsMap.put("nonce", map.get("nonce"));
        paramsMap.put("vlg", "0_0_1");
        paramsMap.put("vData", "");
        JSONObject jsonObject = OkHttpUtils.postJson("https://t.captcha.qq.com/cap_union_new_verify", paramsMap,
                OkHttpUtils.addHeaders("", map.get("showUrl"), UA.QQ));
        if (jsonObject.getInteger("errorCode") == 0){
            TencentCaptcha tencentCaptcha = new TencentCaptcha(jsonObject.getString("ticket"), jsonObject.getString("randstr"));
            return Result.success(tencentCaptcha);
        }else return Result.failure(400, "验证码识别失败，请稍后重试！！");
    }

    public static Result<TencentCaptcha> identify(Long appId, String sid, String refererUrl) throws IOException {
        Map<String, String> map = getCaptcha(appId, sid, refererUrl);
        return identifyCaptcha(appId, sid, map);
    }

}
