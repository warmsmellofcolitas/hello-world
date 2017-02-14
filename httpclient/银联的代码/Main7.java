package aa;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpConnectionManager;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.ByteArrayRequestEntity;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.commons.httpclient.params.HttpMethodParams;

public class Main7 {
	public static void main(String[] args) {
		PostMethod method = new PostMethod("http://localhost:80/entry-http2/abc");
		try {
	    	HttpConnectionManagerParams params = new HttpConnectionManagerParams();
	        params.setConnectionTimeout(15000);
	        params.setSoTimeout(30000);
	        params.setStaleCheckingEnabled(true);
	        params.setTcpNoDelay(true);
	        params.setDefaultMaxConnectionsPerHost(100);
	        params.setMaxTotalConnections(1000);
	        params.setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(0, false)); // 这个设置和下面的method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(0, false))相同
	        
	    	HttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager();
	        connectionManager.setParams(params);
	        HttpClient client = new HttpClient(connectionManager);
	        
			String data = "qn=201312301543120990931&signMethod=MD5&transType=31";
			RequestEntity requestEntity = new ByteArrayRequestEntity(data.getBytes("GBK"));
			method.addRequestHeader("Connection", "Keep-Alive");
	        method.getParams().setCookiePolicy(CookiePolicy.IGNORE_COOKIES);
	        method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(0, false));
	        method.setRequestEntity(requestEntity);
	        method.addRequestHeader("Content-Type","application/x-www-form-urlencoded");
	        int statusCode = client.executeMethod(method);
	        if (statusCode != HttpStatus.SC_OK) {
	            System.out.println("失败");
	        }
	        byte[] response = method.getResponseBody();
	        String responseStr = new String(response, "UTF-8");
	        System.out.println("返回的字符串：" + responseStr);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			 method.releaseConnection();
		}
	}
}
