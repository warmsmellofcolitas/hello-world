package com.bestpay.paycenter.entry.http.controller.biz;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

/**
 * 银行中转站
 * @author 李杰
 */
@Slf4j
public class BankRouterUtil {
    public static void main(String[] args) throws Exception {
	byte[] sendData = {0x01,0x02,0x04,0x04};
        byte[] responseData = send("192.0.0.1", "8080", sendData);

        String responseStr = new String(responseData, "GBK");
        System.out.println("返回的字符串为: " + responseStr);

    }

    /**
     * 银行调用发送
     * @param ip 银行请求ip
     * @param port 银行请求端口
     * @param requestData 银行请求数据
     */
    public static byte[] send(String ip, String port, byte[] requestData) throws Exception {
        log.debug("银行路由--工具类--进入");
        String requestDataStr = ConvertUtil.bytesToHexString(requestData);
        log.debug("银行路由--工具类--发送给银行的字节数组转换成十六进制的字符串为：" + requestDataStr);
        HttpClient httpclient = new DefaultHttpClient();
        registerSSl(httpclient);
        HttpPost httpPost = new HttpPost("http://webpaytestnew.bestpay.com.cn/bankRouter?ip=" + ip + "&port=" + port + "&message=" + requestDataStr);
        // HttpPost httpPost = new HttpPost("http://localhost/entry-http2/abc?ip=" + ip + "&port=" + port + "&message=" + requestDataStr);
        HttpResponse response = httpclient.execute(httpPost);
        HttpEntity entity = response.getEntity();
        String value = EntityUtils.toString(entity);
        EntityUtils.consume(entity);
        System.out.println("银行路由--工具类--从银行返回的字节数组转换成十六进制的字符串为：" + value);
        return ConvertUtil.hexStringToBytes(value);
    }

    public static void registerSSl(HttpClient httpclient) throws Exception {
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
        sslcontext.init(null, new TrustManager[] { tm }, null);
        SSLSocketFactory sf = new SSLSocketFactory(sslcontext, SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
        Scheme https = new Scheme("https", 443, sf);
        httpclient.getConnectionManager().getSchemeRegistry().register(https);
    }

}
