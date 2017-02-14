package com.bestpay.paycenter.commons.util;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.http.*;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * User: zhangleimin
 * Date: 13-12-6
 * Time: 上午11:28
 */
@Slf4j
public class HttpClientUtil {

    private static RequestConfig config;

    private static HttpClientUtil httpClientUtil;

    private HttpClientUtil() {
    }

    public static HttpClientUtil createHttpClientUtil(int connTimeout, int reqTimeout) {
        config = RequestConfig.custom().setConnectTimeout(connTimeout).setSocketTimeout(reqTimeout).build();
        httpClientUtil = new HttpClientUtil();
        return httpClientUtil;
    }

    public static HttpClientUtil createHttpClientUtil(int connTimeout, int reqTimeout, String proxyHost, int proxyPort) {
        config = RequestConfig.custom().setConnectTimeout(connTimeout).setSocketTimeout(reqTimeout)
                .setProxy(new HttpHost(proxyHost, proxyPort)).setAuthenticationEnabled(true).build();
        httpClientUtil = new HttpClientUtil();
        return httpClientUtil;
    }

    public static HttpClientUtil createHttpClientUtil() {
        config = RequestConfig.custom().setConnectTimeout(0).setSocketTimeout(0).build();
        httpClientUtil = new HttpClientUtil();
        return httpClientUtil;
    }

    public String sendByPost(String url, Map<String, String> param) throws Exception {
        HttpClients.createDefault()
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = null;
        CloseableHttpResponse response = null;
        try {
            httpPost = new HttpPost(url);
            List<NameValuePair> paraList = Lists.newArrayList();
            for (String key : param.keySet()) {
                paraList.add(new BasicNameValuePair(key, param.get(key)));
            }
            httpPost.setEntity(new UrlEncodedFormEntity(paraList, Consts.UTF_8));
            httpPost.setConfig(config);
            response = httpClient.execute(httpPost);
            HttpEntity entity = response.getEntity();
            int statusCode = response.getStatusLine().getStatusCode();
            checkArgument(Objects.equal(statusCode, HttpStatus.SC_OK), "响应码状态不是200");
//            httpPost.clone();
            return EntityUtils.toString(entity);
        } finally {
            if (response != null) {
                response.close();
            }
            if (httpPost != null) {
                httpPost.releaseConnection();
            }
            httpClient.close();
        }
    }

    public String sendByGet(String url, Map<String, String> param) throws Exception {
        HttpClient httpClient = HttpClients.createDefault();
        try {
            StringBuffer paramUrl = new StringBuffer();
            for (String key : param.keySet()) {
                paramUrl.append("&").append(key).append("=").append(param.get(key));
            }
            HttpGet httpGet = new HttpGet(url+paramUrl);
            httpGet.setConfig(config);
            HttpResponse response = httpClient.execute(httpGet);
            HttpEntity entity = response.getEntity();
            int statusCode = response.getStatusLine().getStatusCode();
            checkArgument(Objects.equal(statusCode, HttpStatus.SC_OK), "响应码状态不是200");
//            httpPost.clone();
            return EntityUtils.toString(entity);
        } catch (Exception e) {
            log.error("调用HTTP服务异常，{}", e);
            return "";
        }
    }


    /**
     * 执行后台Http请求
     * @param reqUrl 请求URl
     * @param params 请求参数
     * @return
     */
    public static String invokeRequest(String reqUrl, String params) {
        HttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(reqUrl);
        try {
            httpPost.setEntity(new StringEntity(params, "GBK"));
            HttpResponse response = httpClient.execute(httpPost);
            HttpEntity entity = response.getEntity();

            String value = EntityUtils.toString(entity, "GBK");
            EntityUtils.consume(entity);
            return value;
        } catch (Exception e) {
            log.error("http请求异常", e);
            return StringUtils.EMPTY;
        } finally {
            httpPost = null;
        }
    }

}
