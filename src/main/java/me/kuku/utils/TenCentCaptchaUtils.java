package me.kuku.utils;

import com.alibaba.fastjson.JSONObject;
import me.kuku.pojo.Result;
import me.kuku.pojo.TencentCaptcha;
import me.kuku.pojo.UA;
import okhttp3.Headers;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class TenCentCaptchaUtils {

    private final static String en_ua = Base64.getEncoder().encodeToString(UA.CHROME_91.getValue().getBytes(StandardCharsets.UTF_8));

    private static String getCaptchaPictureUrl(Long appId, String imageId, String sess, String sid, int index, int subSid){
        return "https://t.captcha.qq.com/hycdn?index=" + index + "&image=" + imageId + "?aid=" + appId + "&sess=" + sess + "&sid=" + sid + "&img_index=" + index + "&subsid=" + subSid;
    }

    private static Map<String, String> getCollect(String rnd, String suffixUrl, String showUrl) throws IOException {
        String js = OkHttpUtils.getStr("https://t.captcha.qq.com/" + suffixUrl,
                OkHttpUtils.addHeaders("", showUrl, UA.CHROME_91));
        String base64Str = Base64.getEncoder().encodeToString(js.getBytes());
        Map<String, String> map = new HashMap<>();
        map.put("script", base64Str);
        JSONObject jsonObject = OkHttpUtils.postJson("https://api.kukuqaq.com/tool/collect", map);
        String collectData = URLDecoder.decode(jsonObject.getString("collectData"), "utf-8");
        String eks = jsonObject.getString("eks");
        String length = String.valueOf(collectData.length());
        Map<String, String> result = new HashMap<>();
        result.put("collectData", collectData);
        result.put("eks", eks);
        result.put("length", length);
        return result;
    }

    private static int getWidth(String imageAUrl, String imageBUrl, String imageCUrl, String showUrl) {
        InputStream ais = null;
        InputStream bis = null;
        InputStream cis = null;
        try {
            Headers headers = OkHttpUtils.addHeaders("", showUrl, UA.CHROME_91);
            ais = OkHttpUtils.getByteStream(imageAUrl, headers);
            cis = OkHttpUtils.getByteStream(imageCUrl, headers);
            bis = OkHttpUtils.getByteStream(imageBUrl, headers);
            BufferedImage imageA = ImageIO.read(ais);
            BufferedImage imageB = ImageIO.read(bis);
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
        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        } finally {
            IOUtils.close(ais);
            IOUtils.close(bis);
            IOUtils.close(cis);
        }
    }

    private static Map<String, String> getCaptcha(Long appId, String sid, String capCd, String qq, String refererUrl) throws IOException {
        String preHandUrl = "https://t.captcha.qq.com/cap_union_prehandle?aid=" + appId + "&protocol=https&accver=1&showtype=embed&ua=" +
                URLEncoder.encode(en_ua, "utf-8") + "&noheader=1&fb=1&enableDarkMode=0&sid=" + sid + "&grayscale=1&clientype=2&cap_cd=" +
                capCd + "&uid=" + qq + "&wxLang=&lang=zh&entry_url=" +
                URLEncoder.encode(refererUrl, "utf-8") + "&js=%2Ftcaptcha-frame.a75be429.js&subsid=3&callback=_aq_353052&sess=";
        JSONObject jsonObject = OkHttpUtils.getJsonp(preHandUrl,
                OkHttpUtils.addHeaders("", "https://ssl.captcha.qq.com/", UA.CHROME_91));
        String sess = jsonObject.getString("sess");
        sid = jsonObject.getString("sid");
        String createIframeStart = String.valueOf(System.currentTimeMillis());
        String rnd = MyUtils.randomNum(6);
        String showUrl = "https://t.captcha.qq.com/cap_union_new_show?aid=" + appId + "&protocol=https&accver=1&showtype=embed&ua=" +
                URLEncoder.encode(en_ua, "utf-8") + "&noheader=1&fb=1&enableDarkMode=0&sid=" + sid + "&grayscale=1&clientype=2&sess=" + sess
                + "&fwidth=0&wxLang=&tcScale=1&uid=" + qq + "&cap_cd=" + capCd + "&rnd=" + rnd + "&prehandleLoadTime=23&createIframeStart=" + createIframeStart + "&subsid=2";
        String html = OkHttpUtils.getStr(showUrl,
                OkHttpUtils.addHeaders("", "https://xui.ptlogin2.qq.com/", UA.CHROME_91));
        String height = MyUtils.regex("spt:\"", "\"", html);
        sess = MyUtils.regex("sess:\"", "\"", html);
        String collectName = MyUtils.regex("collectdata:\"", "\"", html);
        String imageId = MyUtils.regex("image=", "\"", html);
        String suffixUrl = MyUtils.regex("\"tdc\",\"/", "\"", html);
        String pow = MyUtils.regex("prefix:\"", "\"", html) + "150";
        String nonce = MyUtils.regex("nonce:\"", "\"", html);
        String imageAUrl = getCaptchaPictureUrl(appId, imageId, sess, sid, 1, 3);
        String imageBUrl = getCaptchaPictureUrl(appId, imageId, sess, sid, 0, 5);
        String imageCUrl = getCaptchaPictureUrl(appId, imageId, sess, sid, 2, 4);
        int width = getWidth(imageAUrl, imageBUrl, imageCUrl, showUrl);
        String ans = width + "," + height + ";";
        Map<String, String> collect = getCollect(rnd, suffixUrl, showUrl);
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
        map.put("capCd", capCd);
        map.put("qq", qq);
        map.put("rnd", rnd);
        map.putAll(collect);
        return map;
    }

    private static Result<TencentCaptcha> identifyCaptcha(Long appId, String sid, Map<String, String> map) throws IOException {
        Map<String, String> paramsMap = new HashMap<>();
        paramsMap.put("aid", String.valueOf(appId));
        paramsMap.put("protocol", "https");
        paramsMap.put("accver", "1");
        paramsMap.put("showtype", "embed");
        paramsMap.put("noheader", "1");
        paramsMap.put("ua", en_ua);
        paramsMap.put("fb", "1");
        paramsMap.put("enableDarkMode", "0");
        paramsMap.put("sid", sid);
        paramsMap.put("grayscale", "1");
        paramsMap.put("clientype", "2");
        paramsMap.put("sess", map.get("sess"));
        paramsMap.put("fwidth", "0");
        paramsMap.put("wxLang", "");
        paramsMap.put("tcScale", "1");
        paramsMap.put("uid", map.get("qq"));
        paramsMap.put("cap_cd", map.get("capCd"));
        paramsMap.put("rnd", map.get("rnd"));
        paramsMap.put("prehandleLoadTime", "23");
        paramsMap.put("createIframeStart", map.get("createIframeStart"));
        paramsMap.put("subsid", "4");
        paramsMap.put("cdata", "0");
        paramsMap.put("ans", map.get("ans"));
        paramsMap.put("vsig", "");
        paramsMap.put("websig", "");
        paramsMap.put("subcapclass", "");
        paramsMap.put("pow_answer", map.get("pow"));
        paramsMap.put("pow_calc_time", "6");
        paramsMap.put(map.get("collectName"), map.get("collectData"));
        paramsMap.put("tlg", map.get("length"));
        paramsMap.put("fpinfo", "");
        paramsMap.put("eks", map.get("eks"));
        paramsMap.put("nonce", map.get("nonce"));
        paramsMap.put("vlg", "0_0_1");
        JSONObject jsonObject = OkHttpUtils.postJson("https://t.captcha.qq.com/cap_union_new_verify", paramsMap,
                OkHttpUtils.addHeaders("", map.get("showUrl"), UA.CHROME_91));
        if (jsonObject.getInteger("errorCode") == 0){
            TencentCaptcha tencentCaptcha = new TencentCaptcha(jsonObject.getString("ticket"), jsonObject.getString("randstr"));
            return Result.success(tencentCaptcha);
        }else return Result.failure(400, "验证码识别失败，请稍后重试！！");
    }

    public static Result<TencentCaptcha> identify(Long appId, String sid, String capCd, Long qq, String refererUrl) throws IOException {
        Map<String, String> map = getCaptcha(appId, sid, capCd, qq.toString(), refererUrl);
        return identifyCaptcha(appId, sid, map);
    }

    public static Result<TencentCaptcha> identify(Long appId, String sid, String refererUrl) throws IOException {
        Map<String, String> map = getCaptcha(appId, sid, "", "", refererUrl);
        return identifyCaptcha(appId, sid, map);
    }

}
