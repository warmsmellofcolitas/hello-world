package com.bestpay.paycenter.gateway.bank.utils;

import com.bestpay.paycenter.commons.constants.BankResCode;
import com.bestpay.paycenter.commons.exception.BankException;
import org.apache.commons.lang.StringUtils;
import org.apache.http.*;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.URLDecoder;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Author tanshuai
 *
 * @Modify by shenbobo 2014-05-30
 * Date 2013-4-12 上午10:44:07
 * Version 1.0
 */
public class HttpClientUtil extends Thread {
    private static final Logger logger = LoggerFactory.getLogger(HttpClientUtil.class);

    public static HttpClient httpclient;
    private static PoolingClientConnectionManager cm;

    private static int DEFAULT_CONNECTIN_TIMEOUT = 20000;
    private static int DEFAULT_SO_TIMEOUT = 50000;

    private static String DEFAULT_CHARSET = "UTF-8";

    public static void init() throws Exception {
        logger.info("初始化httpclient");
        cm = new PoolingClientConnectionManager();
        cm.setMaxTotal(100);// 最大连接
        cm.setDefaultMaxPerRoute(10);// 每个路由最大连接

        // HttpHost localhost = new HttpHost("locahost", 80);
        // cm.setMaxPerRoute(new HttpRoute(localhost), 50);// 增加指定路由的最大连接

        HttpParams params = new BasicHttpParams();
        params.setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, DEFAULT_CONNECTIN_TIMEOUT); // 连接超时
        params.setParameter(CoreConnectionPNames.SO_TIMEOUT, DEFAULT_SO_TIMEOUT); // 请求超时
        httpclient = new DefaultHttpClient(cm, params);
        registerSSl();
//        HttpHost proxy = new HttpHost("192.168.19.9", 80, "http");
//        httpclient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);

        Thread thread = new HttpClientUtil();
        thread.setDaemon(true);
        thread.start();
    }

    public static String invoke(String reqUrl) {
        return invoke(reqUrl, StringUtils.EMPTY, DEFAULT_CHARSET, true);

    }

    public static String invoke(String reqUrl, String params) {
        return invoke(reqUrl, params, DEFAULT_CHARSET, true);
    }

    public static String invoke(String reqUrl, String params, boolean decode) {
        return invoke(reqUrl, params, DEFAULT_CHARSET, decode);
    }

    /**
     * http请求
     *
     * @param reqUrl
     * @param params
     * @param charsetName
     * @param decode
     * @return
     */
    public static String invoke(String reqUrl, String params, String charsetName, boolean decode) {
        HttpPost httpPost = new HttpPost(reqUrl);
        try {
            if (!StringUtils.isEmpty(params)) {
                httpPost.setEntity(new StringEntity(params, charsetName));
            }
            HttpResponse response = httpclient.execute(httpPost);
            HttpEntity entity = response.getEntity();

            String value = EntityUtils.toString(entity, charsetName);
            EntityUtils.consume(entity);
            if (decode) {
                value = URLDecoder.decode(value, charsetName);
            }
            return value;
        } catch (ConnectTimeoutException cte) {   //请求超时
            logger.error("请求超时{}", cte);
            throw new BankException(BankResCode.BF_CNCT_REQUESTOUT);
        } catch (SocketTimeoutException ste) {  //读取超时
            logger.error("读取超时{}", ste);
            throw new BankException(BankResCode.BF_CNCT_READOUT);
        } catch (Exception e) {
            logger.error("", e);
            return StringUtils.EMPTY;
        } finally {
            httpPost = null;
        }
    }

    public static String invoke(String reqUrl, String params, String charsetName, boolean decode, String mimeType) {
        HttpPost httpPost = new HttpPost(reqUrl);
        try {
            if (!StringUtils.isEmpty(params)) {
                httpPost.setEntity(new StringEntity(params, ContentType.create(mimeType, charsetName)));
            }
            HttpResponse response = httpclient.execute(httpPost);
            HttpEntity entity = response.getEntity();

            String value = EntityUtils.toString(entity, charsetName);
            EntityUtils.consume(entity);
            if (decode) {
                value = URLDecoder.decode(value, charsetName);
            }
            return value;
        } catch (ConnectTimeoutException cte) {   //请求超时
            logger.error("请求超时{}", cte);
            throw new BankException(BankResCode.BF_CNCT_REQUESTOUT);
        } catch (SocketTimeoutException ste) {  //读取超时
            logger.error("读取超时{}", ste);
            throw new BankException(BankResCode.BF_CNCT_READOUT);
        } catch (Exception e) {
            logger.error("", e);
            return StringUtils.EMPTY;
        } finally {
            httpPost = null;
        }
    }

    public static String invoke(String reqUrl, Map<String, String> params) {
        return invoke(reqUrl, params, DEFAULT_CHARSET, true);
    }

    public static String invoke(String reqUrl, Map<String, String> params, boolean decode) {
        return invoke(reqUrl, params, DEFAULT_CHARSET, decode);
    }

    /**
     * http 请求Map参数
     *
     * @param reqUrl
     * @param params
     * @param charset
     * @param decode
     * @return
     */
    public static String invoke(String reqUrl, Map<String, String> params, String charset, boolean decode) {
        HttpPost httpPost = new HttpPost(reqUrl);
        try {
            if (null != params) {
                List<NameValuePair> nvps = new ArrayList<NameValuePair>(params.size());
                Set<Entry<String, String>> set = params.entrySet();
                for (Entry<String, String> entry : set) {
                    nvps.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
                }
                httpPost.setEntity(new UrlEncodedFormEntity(nvps, charset));
            }

            HttpResponse response = httpclient.execute(httpPost);
            HttpEntity entity = response.getEntity();

            String value = EntityUtils.toString(entity, charset);
            EntityUtils.consume(entity);
            if (decode) {
                value = URLDecoder.decode(value, charset);
            }
            return value;
        } catch (ConnectTimeoutException cte) {   //请求超时
            logger.error("请求超时{}", cte);
            throw new BankException(BankResCode.BF_CNCT_REQUESTOUT);
        } catch (SocketTimeoutException ste) {  //读取超时
            logger.error("读取超时{}", ste);
            throw new BankException(BankResCode.BF_CNCT_READOUT);
        } catch (Exception e) {
            logger.error("", e);
            return StringUtils.EMPTY;
        } finally {
            httpPost = null;
        }
    }

    @Override
    public void run() {
        try {
            while (true) {
                TimeUnit.MINUTES.sleep(3);// 每隔3分钟清理连接
                if (cm != null) {
                    cm.closeExpiredConnections();
                    cm.closeIdleConnections(20, TimeUnit.MINUTES);// 连接空闲20分钟后被关闭
                }
            }
        } catch (InterruptedException e) {
            logger.error("", e);
        }
    }

    public static void registerSSl() throws Exception {
        SSLContext sslcontext = SSLContext.getInstance("TLS");
        X509TrustManager tm = new X509TrustManager() {
            @Override
            public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
            }

            @Override
            public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }
        };
        sslcontext.init(null, new TrustManager[]{tm}, null);
        SSLSocketFactory sf = new SSLSocketFactory(sslcontext, SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
        Scheme https = new Scheme("https", 443, sf);

        httpclient.getConnectionManager().getSchemeRegistry().register(https);
    }

    public static void shutdown() {
        if (cm != null) {
            cm.shutdown();
        }
    }

    /**
     * https  请求，第一组key对应于keystores  第二组对应truststores
     *
     * @param urlContent
     * @param keyPath
     * @param keyPwd
     * @param trustKeyPath
     * @param trustKeyPwd
     * @return
     */
    public static String invokeGetHttps(String urlContent, String keyPath, String keyPwd, String trustKeyPath, String trustKeyPwd) {
        CloseableHttpClient httpclient = null;
        try {
            httpclient = getCloseableHttpClient(keyPath, keyPwd, trustKeyPath, trustKeyPwd);
//            HttpHost proxy = new HttpHost("192.168.19.9", 80, "http");
            RequestConfig config = RequestConfig.custom()
//                    .setProxy(proxy)
                    .setSocketTimeout(DEFAULT_SO_TIMEOUT)
                    .setConnectTimeout(DEFAULT_CONNECTIN_TIMEOUT)
                    .setConnectionRequestTimeout(DEFAULT_CONNECTIN_TIMEOUT)
                    .build();
            HttpGet httpget = new HttpGet(urlContent);
            httpget.setConfig(config);
            CloseableHttpResponse response = httpclient.execute(httpget);
            try {
                return EntityUtils.toString(response.getEntity());
            } finally {
                EntityUtils.consume(response.getEntity());
                response.close();
            }
        } catch (ConnectTimeoutException cte) {   //请求超时
            logger.error("请求超时{}", cte);
            throw new BankException(BankResCode.BF_CNCT_REQUESTOUT);
        } catch (SocketTimeoutException ste) {  //读取超时
            logger.error("读取超时{}", ste);
            throw new BankException(BankResCode.BF_CNCT_READOUT);
        } catch (Exception e) {
            logger.error("", e);
            return StringUtils.EMPTY;
        } finally {
            if (null != httpclient) {
                try {
                    httpclient.close();
                } catch (IOException e) {
                }
            }

        }
    }

    private static CloseableHttpClient getCloseableHttpClient(String keyPath, String keyPwd, String trustKeyPath, String trustKeyPwd) throws KeyStoreException,
            IOException, NoSuchAlgorithmException, CertificateException, KeyManagementException, UnrecoverableKeyException {
        KeyStore ks = KeyStore.getInstance("PKCS12");
        FileInputStream keyInStream = new FileInputStream(new File(keyPath));
        try {
            ks.load(keyInStream, keyPwd.toCharArray());
        } finally {
            keyInStream.close();
        }
        KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
        FileInputStream trustInStream = new FileInputStream(new File(trustKeyPath));
        try {
            trustStore.load(trustInStream, trustKeyPwd.toCharArray());
        } finally {
            trustInStream.close();
        }
        // Trust own CA and all self-signed certs
        SSLContext sslcontext = SSLContexts.custom()
                .loadTrustMaterial(trustStore, new TrustSelfSignedStrategy())
                .loadKeyMaterial(ks, keyPwd.toCharArray())
                .build();
        // Allow TLSv1 protocol only
        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
                sslcontext,
                new String[]{"TLSv1"},
                null,
                SSLConnectionSocketFactory.BROWSER_COMPATIBLE_HOSTNAME_VERIFIER);
        return HttpClients.custom()
                .setSSLSocketFactory(sslsf)
                .build();
    }
}
