package me.kuku.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import me.kuku.pojo.UA;
import okhttp3.*;
import okio.ByteString;

import javax.net.ssl.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("unused")
public class OkHttpUtils {
    private static final MediaType MEDIA_JSON = MediaType.get("application/json;charset=utf-8");
    private static final MediaType MEDIA_STREAM = MediaType.get("application/octet-stream");
    private static final MediaType MEDIA_X_JSON = MediaType.get("text/x-json");
    private static final MediaType MEDIA_ENCRYPTED_JSON = MediaType.get("application/encrypted-json;charset=UTF-8");
    private static final MediaType MEDIA_TEXT = MediaType.get("text/plain;charset=UTF-8");
    private static final long TIME_OUT = 10L;

    private static final OkHttpClient okHttpClient;

    static {
        final SSLContext sslContext;
        TrustManager[] trustAllCerts = null;
        javax.net.ssl.SSLSocketFactory sslSocketFactory = null;
        try {
            trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) {
                        }

                        @Override
                        public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) {
                        }

                        @Override
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return new java.security.cert.X509Certificate[]{};
                        }
                    }
            };
            sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            sslSocketFactory = sslContext.getSocketFactory();
        } catch (Exception ignore) {
        }
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .followRedirects(false);
        if (sslSocketFactory != null) {
            builder = builder.sslSocketFactory(sslSocketFactory, (X509TrustManager) (trustAllCerts[0]));
        }
        okHttpClient = builder.hostnameVerifier((String hostname, SSLSession session) -> true)
                .followSslRedirects(false)
                .connectTimeout(TIME_OUT, TimeUnit.SECONDS)
                .readTimeout(TIME_OUT, TimeUnit.SECONDS).build();
    }

    private static Headers emptyHeaders(){
        return addSingleHeader("User-Agent", UA.PC.getValue());
    }

    public static Response get(String url, Headers headers) throws IOException {
        Request request = new Request.Builder().url(url).headers(headers).build();
        return okHttpClient.newCall(request).execute();
    }

    public static Response get(String url, Map<String, String> map) throws IOException {
        return get(url, addHeaders(map));
    }

    public static Response get(String url) throws IOException {
        return get(url, emptyHeaders());
    }

    public static Response post(String url, RequestBody requestBody, Headers headers) throws IOException {
        Request request = new Request.Builder().url(url).post(requestBody).headers(headers).build();
        return okHttpClient.newCall(request).execute();
    }

    public static Response post(String url, RequestBody requestBody) throws IOException {
        return post(url, requestBody, emptyHeaders());
    }

    public static Response post(String url, Map<String, String> map, Headers headers) throws IOException {
        return post(url, mapToFormBody(map), headers);
    }

    public static Response post(String url, Map<String, String> map, Map<String, String> headerMap) throws IOException {
        return post(url, mapToFormBody(map), addHeaders(headerMap));
    }

    public static Response post(String url, RequestBody requestBody, Map<String, String> headerMap) throws IOException {
        return post(url, requestBody, addHeaders(headerMap));
    }

    public static Response post(String url, Map<String, String> map) throws IOException {
        return post(url, map, emptyHeaders());
    }

    public static Response post(String url) throws IOException {
        return post(url, new HashMap<>(), emptyHeaders());
    }

    public static Response put(String url, RequestBody requestBody, Headers headers) throws IOException {
        Request request = new Request.Builder().url(url).put(requestBody).headers(headers).build();
        return okHttpClient.newCall(request).execute();
    }

    public static Response put(String url, RequestBody requestBody) throws IOException {
        return put(url, requestBody, emptyHeaders());
    }

    public static Response put(String url, Map<String, String> map) throws IOException {
        return put(url, mapToFormBody(map), emptyHeaders());
    }

    public static Response put(String url, Map<String, String> map, Map<String, String> headers) throws IOException {
        return put(url, mapToFormBody(map), addHeaders(headers));
    }

    public static Response put(String url, Map<String, String> map, Headers headers) throws IOException {
        return put(url, mapToFormBody(map), headers);
    }

    private static Response delete(String url, RequestBody requestBody, Headers headers) throws IOException {
        Request request = new Request.Builder().url(url).delete(requestBody).headers(headers).build();
        return okHttpClient.newCall(request).execute();
    }

    private static Response delete(String url, RequestBody requestBody) throws IOException {
        return delete(url, requestBody, emptyHeaders());
    }

    public static Response delete(String url, Map<String, String> map, Headers headers) throws IOException {
        return delete(url, mapToFormBody(map), headers);
    }

    public static Response delete(String url, Map<String, String> map, Map<String, String> headerMap) throws IOException {
        return delete(url, mapToFormBody(map), addHeaders(headerMap));
    }

    public static Response delete(String url, RequestBody requestBody, Map<String, String> headerMap) throws IOException {
        return delete(url, requestBody, addHeaders(headerMap));
    }

    public static Response delete(String url, Map<String, String> map) throws IOException {
        return delete(url, map, emptyHeaders());
    }

    public static Response patch(String url, RequestBody requestBody, Headers headers) throws IOException {
        Request request = new Request.Builder().url(url).patch(requestBody).headers(headers).build();
        return okHttpClient.newCall(request).execute();
    }

    public static Response patch(String url, RequestBody requestBody) throws IOException {
        return patch(url, requestBody, emptyHeaders());
    }

    public static Response patch(String url, Map<String, String> map, Headers headers) throws IOException {
        return patch(url, mapToFormBody(map), headers);
    }

    public static Response patch(String url, Map<String, String> map, Map<String, String> headerMap) throws IOException {
        return patch(url, mapToFormBody(map), addHeaders(headerMap));
    }

    public static Response patch(String url, Map<String, String> map) throws IOException {
        return patch(url, map, emptyHeaders());
    }

    public static String getStr(Response response) throws IOException {
        return Objects.requireNonNull(response.body()).string();
    }

    public static JSONObject getJson(Response response) throws IOException {
        String str = getStr(response);
        return JSON.parseObject(str);
    }

    public static byte[] getBytes(String url) throws IOException {
        return getBytes(url, emptyHeaders());
    }

    public static byte[] getBytes(String url, Headers headers) throws IOException {
        Response response = get(url, headers);
        return getBytes(response);
    }

    public static byte[] getBytes(String url, Map<String, String> headers) throws IOException {
        Response response = get(url, headers);
        return getBytes(response);
    }

    public static byte[] getBytes(Response response) throws IOException {
        return Objects.requireNonNull(response.body()).bytes();
    }

    public static InputStream getByteStream(Response response){
        return Objects.requireNonNull(response.body()).byteStream();
    }

    public static InputStream getByteStream(String url, Headers headers) throws IOException {
        Response response = get(url, headers);
        return getByteStream(response);
    }

    public static InputStream getByteStream(String url, Map<String, String> headers) throws IOException {
        Response response = get(url, headers);
        return getByteStream(response);
    }

    public static InputStream getByteStream(String url) throws IOException {
        return getByteStream(url, emptyHeaders());
    }

    private static ByteString getByteStr(Response response) throws IOException {
        return Objects.requireNonNull(response.body()).byteString();
    }

    public static ByteString getByteStr(String url, Headers headers) throws IOException {
        Response response = get(url, headers);
        return getByteStr(response);
    }

    public static ByteString getByteStr(String url, Map<String, String> headers) throws IOException {
        Response response = get(url, headers);
        return getByteStr(response);
    }

    public static ByteString getByteStr(String url) throws IOException {
        return getByteStr(url, emptyHeaders());
    }

    private static InputStream getIs(Response response){
        return Objects.requireNonNull(response.body()).byteStream();
    }

    public static String getStr(String url, Headers headers) throws IOException {
        Response response = get(url, headers);
        return getStr(response);
    }

    public static String getStr(String url, Map<String, String> headers) throws IOException {
        Response response = get(url, headers);
        return getStr(response);
    }

    public static String getStr(String url) throws IOException {
        Response response = get(url, emptyHeaders());
        return getStr(response);
    }

    public static JSONObject getJson(String url, Headers headers) throws IOException {
        Response response = get(url, headers);
        return getJson(response);
    }

    public static JSONObject getJson(String url, Map<String, String> map) throws IOException {
        return getJson(url, addHeaders(map));
    }

    public static JSONObject getJson(String url) throws IOException {
        Response response = get(url, emptyHeaders());
        return getJson(response);
    }

    public static String postStr(String url, RequestBody requestBody, Headers headers) throws IOException {
        Response response = post(url, requestBody, headers);
        return getStr(response);
    }

    public static String postStr(String url, RequestBody requestBody, Map<String, String> headers) throws IOException {
        Response response = post(url, requestBody, headers);
        return getStr(response);
    }

    public static String postStr(String url, RequestBody requestBody) throws IOException {
        Response response = post(url, requestBody, emptyHeaders());
        return getStr(response);
    }

    public static JSONObject postJson(String url, RequestBody requestBody, Headers headers) throws IOException {
        Response response = post(url, requestBody, headers);
        return getJson(response);
    }

    public static JSONObject postJson(String url, RequestBody requestBody, Map<String, String> headers) throws IOException {
        Response response = post(url, requestBody, headers);
        return getJson(response);
    }

    public static JSONObject postJson(String url, RequestBody requestBody) throws IOException {
        Response response = post(url, requestBody, emptyHeaders());
        return getJson(response);
    }

    private static RequestBody mapToFormBody(Map<String, String> map){
        FormBody.Builder builder = new FormBody.Builder();
        for (Map.Entry<String, String> entry: map.entrySet()){
            builder.add(entry.getKey(), entry.getValue());
        }
        return builder.build();
    }

    public static String postStr(String url, Map<String, String> map, Headers headers) throws IOException {
        Response response = post(url, mapToFormBody(map), headers);
        return getStr(response);
    }

    public static String postStr(String url, Map<String, String> map,Map<String, String> headers) throws IOException {
        Response response = post(url, mapToFormBody(map), headers);
        return getStr(response);
    }

    public static String postStr(String url, Map<String, String> map) throws IOException {
        return postStr(url, map, emptyHeaders());
    }

    public static String deleteStr(String url, Map<String, String> map, Headers headers) throws IOException {
        Response response = delete(url, mapToFormBody(map), headers);
        return getStr(response);
    }

    public static String deleteStr(String url, Map<String, String> map, Map<String, String> headers) throws IOException {
        Response response = delete(url, mapToFormBody(map), headers);
        return getStr(response);
    }

    public static String deleteStr(String url, Map<String, String> map) throws IOException {
        return deleteStr(url, map, emptyHeaders());
    }

    public static JSONObject postJson(String url, Map<String, String> map, Headers headers) throws IOException {
        String str = postStr(url, map, headers);
        return JSON.parseObject(str);
    }

    public static JSONObject postJson(String url, Map<String, String> map, Map<String, String> headers) throws IOException {
        String str = postStr(url, map, headers);
        return JSON.parseObject(str);
    }

    public static JSONObject postJson(String url, Map<String, String> map) throws IOException {
        String str = postStr(url, map, emptyHeaders());
        return JSON.parseObject(str);
    }

    public static JSONObject deleteJson(String url, Map<String, String> map, Headers headers) throws IOException {
        String str = deleteStr(url, map, headers);
        return JSON.parseObject(str);
    }

    public static JSONObject deleteJson(String url, Map<String, String> map, Map<String, String> headers) throws IOException {
        String str = deleteStr(url, map, headers);
        return JSON.parseObject(str);
    }

    public static JSONObject deleteJson(String url, Map<String, String> map) throws IOException {
        String str = deleteStr(url, map, emptyHeaders());
        return JSON.parseObject(str);
    }

    public static JSONObject getJsonp(Response response) throws IOException {
        String str = getStr(response);
        Matcher matcher = Pattern.compile("\\{[\\s\\S]*}").matcher(str);
        if (matcher.find()){
            return JSON.parseObject(matcher.group());
        }else return null;
    }

    public static JSONObject getJsonp(String url, Headers headers) throws IOException {
        Response response = get(url, headers);
        return getJsonp(response);
    }

    public static JSONObject getJsonp(String url) throws IOException {
        return getJsonp(url, emptyHeaders());
    }

    public static RequestBody addJson(String jsonStr){
        return RequestBody.create(jsonStr, MEDIA_JSON);
    }

    public static RequestBody addText(String text){
        return RequestBody.create(text, MEDIA_TEXT);
    }

    public static RequestBody addBody(String text, String contentType){
        MediaType mediaType = MediaType.get(contentType);
        return RequestBody.create(text, mediaType);
    }

    public static RequestBody addJson(JSONObject jsonObject){
        return RequestBody.create(jsonObject.toJSONString(), MEDIA_JSON);
    }

    public static RequestBody addEncryptedJson(String str){
        return RequestBody.create(str, MEDIA_ENCRYPTED_JSON);
    }

    public static Headers addSingleHeader(String name, String value){
        return new Headers.Builder().add(name, value).build();
    }

    public static Headers addHeaders(String cookie, String referer, String userAgent){
        if (cookie == null) cookie = "";
        if (referer == null) referer = "";
        if (userAgent == null) userAgent = UA.PC.getValue();
        return new Headers.Builder().add("Cookie", cookie).add("Referer", referer).add("User-Agent", userAgent).build();
    }

    public static Headers addHeaders(String cookie, String referer, UA ua){
        return addHeaders(cookie, referer, ua.getValue());
    }

    public static Headers addHeaders(String cookie, String referer){
        return addHeaders(cookie, referer, UA.PC.getValue());
    }

    public static Headers addHeaders(Map<String, String> map){
        Headers.Builder builder = new Headers.Builder();
        for (Map.Entry<String, String> entry: map.entrySet()){
            builder.add(entry.getKey(), entry.getValue());
        }
        return builder.build();
    }

    public static Headers.Builder addHeader(){
        return new Headers.Builder();
    }

    public static Headers addUA(UA ua){
        return addSingleHeader("User-Agent", ua.getValue());
    }

    public static Headers addUA(String ua){
        return addSingleHeader("User-Agent", ua);
    }

    public static Headers addCookie(String cookie){
        return addSingleHeader("Cookie", cookie);
    }

    public static Headers addReferer(String url){
        return addSingleHeader("Referer", url);
    }

    public static RequestBody addStream(ByteString byteString){
        return RequestBody.create(byteString, MEDIA_STREAM);
    }

    public static RequestBody addStream(String url) throws IOException {
       return addStream(getByteStr(url));
    }

    public static String getCookie(Response response){
        StringBuilder sb = new StringBuilder();
        List<String> cookies = response.headers("Set-Cookie");
        for (String cookie: cookies){
            if (cookie.contains("deleted")) continue;
            cookie = MyUtils.regex(".*?;", cookie);
            if (cookie == null) continue;
            String[] arr = cookie.split("=");
            if (arr.length < 2) continue;
            if (arr[1].equals(";")) continue;
            sb.append(cookie).append(" ");
        }
        return sb.toString();
    }

    public static String getCookie(Response response, String name){
        String cookie = getCookie(response);
        return getCookie(cookie, name);
    }

    public static Map<String, String> getCookie(Response response, String...name){
        String cookie = getCookie(response);
        return getCookie(cookie, name);
    }

    public static String getCookieStr(Response response, String...name){
        String cookie = getCookie(response);
        return getCookieStr(cookie, name);
    }

    public static String getCookieStr(String cookie, String...name){
        StringBuilder sb = new StringBuilder();
        for (String str: name){
            String singleCookie = getCookieStr(cookie, str);
            sb.append(singleCookie);
        }
        return sb.toString();
    }

    private static String getCookieStr(String cookie, String name){
        return MyUtils.regex(name + "=[^;]+; ", cookie);
    }

    public static String getCookie(String cookie, String name){
        String[] arr = cookie.split("; ");
        if (arr.length == 0) arr = cookie.split(";");
        for (String str : arr) {
            String[] newArr = str.split("=");
            if (newArr.length > 1 && newArr[0].trim().equals(name)){
                return str.substring(str.indexOf('=') + 1);
            }
        }
        return null;
    }

    public static Map<String, String> getCookie(String cookie, String...name){
        Map<String, String> map = new HashMap<>();
        for (String str: name){
            map.put(str, getCookie(cookie, str));
        }
        return map;
    }

    public static Map<String, String> cookieToMap(String cookie){
        Map<String, String> map = new HashMap<>();
        String[] arr = cookie.split(";");
        for (String str: arr){
            String[] newArr = str.split("=");
            if (newArr.length > 1)
                map.put(newArr[0].trim(), newArr[1].trim());
        }
        return map;
    }

    public static String getCookie(String url, Headers headers) throws IOException {
        Response response = get(url, headers);
        return getCookie(response);
    }

    public static String getCookie(String url) throws IOException {
        return getCookie(url, emptyHeaders());
    }

    private static Response download(String url) throws IOException {
        Response response;
        while (true){
            response = get(url);
            int code = response.code();
            if (code == 302 || code == 301){
                response.close();
                url = response.header("location");
            }else break;
        }
        return response;
    }

    public static String downloadStr(String url) throws IOException {
        Response response = download(url);
        return getStr(response);
    }

    public static byte[] downloadBytes(String url) throws IOException {
        Response response = download(url);
        return getBytes(response);
    }

    private static String fileNameByUrl(String url){
        int index = url.lastIndexOf("/");
        return url.substring(index + 1);
    }

    public static RequestBody getStreamBody(String url) throws IOException {
        InputStream is = getByteStream(url);
        return getStreamBody(fileNameByUrl(url), is);
    }

    public static RequestBody getStreamBody(File file){
        return RequestBody.create(file, MEDIA_STREAM);
    }

    public static RequestBody getStreamBody(File file, String contentType){
        return RequestBody.create(file, MediaType.get(contentType));
    }

    public static RequestBody getStreamBody(String fileName, InputStream is){
        try {
            File file = IOUtils.writeTmpFile(fileName, is);
            return RequestBody.create(file, MEDIA_STREAM);
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static RequestBody getStreamBody(InputStream is){
        return getStreamBody(UUID.randomUUID().toString(), is);
    }

    public static RequestBody getStreamBody(String fileName, byte[] bytes){
        return RequestBody.create(bytes, MEDIA_STREAM);
    }

    public static RequestBody getStreamBody(byte[] bytes){
        return getStreamBody(UUID.randomUUID().toString(), bytes);
    }

    public static String urlParams(Map<String, String> map){
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry: map.entrySet()){
            try {
                sb.append(entry.getKey()).append("=").append(URLEncoder.encode(entry.getValue(), "utf-8")).append("&");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }

    public static String urlParamsEn(Map<String, String> map){
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry: map.entrySet()){
            sb.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
        }
        return sb.toString();
    }
}
