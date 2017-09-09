package com.mcu32.firstq.wechat.tools.liufeng.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.mcu32.firstq.wechat.tools.liufeng.bean.AccessToken;
import com.mcu32.firstq.wechat.tools.liufeng.bean.Menu;

/** 
 * 公众平台通用接口工具类 
 *  
 * @author liuyq 
 * @date 2013-08-09 
 */  
public class WeixinUtil {

	private static Logger log = LoggerFactory.getLogger(WeixinUtil.class);  
	  
    /** 
     * 发起https请求并获取结果 
     *  
     * @param requestUrl 请求地址 
     * @param requestMethod 请求方式（GET、POST） 
     * @param outputStr 提交的数据 
     * @return JSONObject(通过JSONObject.get(key)的方式获取json对象的属性值) 
     */  
    public static JSONObject httpRequest(String requestUrl, String requestMethod, String outputStr) {  
        JSONObject jsonObject = null;  
        StringBuffer buffer = new StringBuffer();
        try {
            // 创建SSLContext对象，并使用我们指定的信任管理器初始化  
            TrustManager[] tm = { new MyX509TrustManager() };  
            SSLContext sslContext = SSLContext.getInstance("SSL", "SunJSSE");  
            sslContext.init(null, tm, new java.security.SecureRandom());  
            // 从上述SSLContext对象中得到SSLSocketFactory对象  
            SSLSocketFactory ssf = sslContext.getSocketFactory();  
  
            URL url = new URL(requestUrl);  
            HttpsURLConnection httpUrlConn = (HttpsURLConnection) url.openConnection();  
            httpUrlConn.setSSLSocketFactory(ssf);  
  
            httpUrlConn.setDoOutput(true);  
            httpUrlConn.setDoInput(true);  
            httpUrlConn.setUseCaches(false);  
            // 设置请求方式（GET/POST）  
            httpUrlConn.setRequestMethod(requestMethod);  
  
            if ("GET".equalsIgnoreCase(requestMethod))  
                httpUrlConn.connect();  
  
            // 当有数据需要提交时  
            if (null != outputStr) {
                OutputStream outputStream = httpUrlConn.getOutputStream();  
                // 注意编码格式，防止中文乱码  
                outputStream.write(outputStr.getBytes("UTF-8"));  
                outputStream.close();  
            }  
  
            // 将返回的输入流转换成字符串  
            InputStream inputStream = httpUrlConn.getInputStream();  
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "utf-8");  
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            String str = null;  
            while ((str = bufferedReader.readLine()) != null) {  
                buffer.append(str);  
            }  
            bufferedReader.close();  
            inputStreamReader.close();  
            // 释放资源  
            inputStream.close();  
            inputStream = null;  
            httpUrlConn.disconnect();
            jsonObject = JSON.parseObject(buffer.toString());  
        } catch (ConnectException ce) {  
            log.error("Weixin server connection timed out.");  
        } catch (Exception e) {  
            log.error("https request error:{}", e);  
        }  
        return jsonObject;  
    }  
    
 // 获取access_token的接口地址（GET） 限200（次/天）  
    public final static String access_token_url = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=APPID&secret=APPSECRET";  
    /**
     * 获取access_token 
     * @param appid 凭证 
     * @param appsecret 密钥 
     * @return 
     */
    public static AccessToken getAccessToken(String appid, String appsecret) {  
        AccessToken accessToken = null;  
      
        String requestUrl = access_token_url.replace("APPID", appid).replace("APPSECRET", appsecret);  
        JSONObject jsonObject = httpRequest(requestUrl, "GET", null);
        // 如果请求成功  
        if (null != jsonObject) {  
            try {  
                accessToken = new AccessToken();  
                accessToken.setToken(jsonObject.getString("access_token"));  
                accessToken.setExpiresIn(jsonObject.getIntValue("expires_in"));  
            } catch (JSONException e) {  
                accessToken = null;  
                // 获取token失败  
                log.error("获取token失败 errcode:{} errmsg:{}", jsonObject.getIntValue("errcode"), jsonObject.getString("errmsg"));  
            }  
        }  
        return accessToken;  
    }  
    

    
 // 菜单创建（POST） 限100（次/天）  
    public static String menu_create_url = "https://api.weixin.qq.com/cgi-bin/menu/create?access_token=ACCESS_TOKEN";  
      
    /** 
     * 创建菜单 
     *  
     * @param menu 菜单实例 
     * @param accessToken 有效的access_token 
     * @return 0表示成功，其他值表示失败 
     */  
    public static int createMenu(Menu menu, String accessToken) {
        int result = 0;  
      
        // 拼装创建菜单的url  
        String url = menu_create_url.replace("ACCESS_TOKEN", accessToken);  
        // 将菜单对象转换成json字符串  
        String jsonMenu = JSON.toJSONString(menu);
        //log.info(jsonMenu);
        
        // 调用接口创建菜单  
        JSONObject jsonObject = httpRequest(url, "POST", jsonMenu);  
        if (null != jsonObject) {  
            if (0 != jsonObject.getIntValue("errcode")) {  
                result = jsonObject.getIntValue("errcode");  
                log.error("创建菜单失败 errcode:{} errmsg:{}", jsonObject.getIntValue("errcode"), jsonObject.getString("errmsg"));  
            }  
        }  
      
        return result;  
    }
    
    /** 
     * 创建菜单 
     *  
     * @param menu 菜单实例 
     * @param accessToken 有效的access_token 
     * @return 0表示成功，其他值表示失败 
     */  
    public static int createMenu(String jsonMenu, String accessToken) {
        int result = 0;  
      
        // 拼装创建菜单的url  
        String url = menu_create_url.replace("ACCESS_TOKEN", accessToken);  
        // 将菜单对象转换成json字符串  
        // 调用接口创建菜单  
        JSONObject jsonObject = httpRequest(url, "POST", jsonMenu);  
      
        if (null != jsonObject) {  
            if (0 != jsonObject.getIntValue("errcode")) {  
                result = jsonObject.getIntValue("errcode");  
                log.error("创建菜单失败 errcode:{} errmsg:{}", jsonObject.getIntValue("errcode"), jsonObject.getString("errmsg"));  
            }  
        }  
      
        return result;  
    }  
    
    //通过OAuth2.0方式不弹出授权页面获得用户基本信息
    /*//原view请求url,如http://mascot.duapp.com/oauth2.php
    //构造请求url如下：https://open.weixin.qq.com/connect/oauth2/authorize?appid=wx8888888888888888&redirect_uri=http://mascot.duapp.com/oauth2.php&response_type=code&scope=snsapi_userinfo&state=1#wechat_redirect
    public static String unConstructionUrl = "https://open.weixin.qq.com/connect/oauth2/authorize?appid=APPID&redirect_uri=REDIRECTURI&response_type=code&scope=snsapi_base&state=1#wechat_redirect";
    //获取构造请求url
    public static String getConstructionUrl(String appid, String redirectUri, String responseType, String scope, String state, String wechatRedirect) {
    	String constructionUrl = unConstructionUrl.replaceAll("APPID", appid).replaceAll("REDIRECTURI", redirectUri);
    	if(wechatRedirect != null && !"".equals(wechatRedirect)) {
    		constructionUrl = constructionUrl.replaceAll("#wechat_redirect", wechatRedirect);
    	} else {
    		constructionUrl = constructionUrl.replaceAll("#wechat_redirect", "");
    	}
    	return constructionUrl;
    }*/
    
    //获取openId的微信接口的url
    public static String getOpenIdUrl = "https://api.weixin.qq.com/sns/oauth2/access_token?appid=APPID&secret=SECRET&code=CODE&grant_type=authorization_code";
    //使用code获取OpenId
    public static String getOpenIdUrl(String appid, String secret, String code) {
    	String openIdUrl = "";
    	if(code != null && !"".equals(code)) {
    		openIdUrl = getOpenIdUrl.replaceAll("APPID", appid).replaceAll("SECRET", secret).replaceAll("CODE", code);
    	}
    	
    	return openIdUrl;
    }
    
    //获取openId
    public static String getOpenId(String appid, String secret, String code) {
    	String openId = "";
    	String requestUrl = getOpenIdUrl(appid, secret, code);
    	JSONObject jsonObject = null;
    	if(requestUrl != null && !"".equals(requestUrl)) {
    		jsonObject = httpRequest(requestUrl, "GET", null);
    	}
    	
    	if(jsonObject != null && !"".equals(jsonObject)) {
    		openId = jsonObject.getString("openid");
    	}
    	return openId;
    }
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    

}
