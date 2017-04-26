package org.springframework.cloud.npdiscovery.util;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RegisterHttpClient {

    private static final Logger LOG = LoggerFactory.getLogger(RegisterHttpClient.class);

    private static RegisterHttpClient RegisterHttpClient;
    private PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
    private static String ENCODEING = "UTF-8";

    private CloseableHttpClient client;

    /**
     * 连接超时时间
     */
    private static final int CONNECT_TIME_OUT = 3000;

    /**
     * 获取连接池中请求的超时时间，不等待
     */
    private static final int CONNECT_REQUEST_TIME_OUT = 1;

    /**
     * 读取数据的socket超时时间
     */
    private static final int SO_TIME_OUT = 3000;

    /**
     * 总计允许50个连接
     */
    private static final int MAX_TOTAL_CONN = 5;

    /**
     * 每个主机最多允许5条连接
     */
    private static final int DEFAULT_MAX_PERROUTE = 2;

    private RegisterHttpClient() {
            super();

            connectionManager.setMaxTotal(MAX_TOTAL_CONN);
            connectionManager.setDefaultMaxPerRoute(DEFAULT_MAX_PERROUTE);

            RequestConfig config = RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD).setConnectTimeout(CONNECT_TIME_OUT)
                    .setConnectionRequestTimeout(CONNECT_REQUEST_TIME_OUT).setSocketTimeout(SO_TIME_OUT).build();
            client = HttpClients.custom().setConnectionManager(connectionManager).setDefaultRequestConfig(config).build();

            startIdleConnectionMonitorThread(connectionManager);
            LOG.info("初始化http连接池,maxtotal:" + MAX_TOTAL_CONN + ",default_max_perroute:" + DEFAULT_MAX_PERROUTE);
        }

    public static RegisterHttpClient getInstance() {
        if (RegisterHttpClient == null) {
            RegisterHttpClient = new RegisterHttpClient();
        }

        return RegisterHttpClient;
    }

    /**
     * 通过GET方式发送请求获取数据
     *
     * @param url
     *            url地址("http://192.168.60.39:8080/pvd/get_incident_info")
     * @param params
     *            参数("orgId="+1+"&page_size=10")
     * @return json串
     */
    public String httpGet(String url, String params) {
        return httpGet(url, params, null);
    }

    public String httpGet(String url, String params, Map<String, String> headers) {
        HttpGet httpGet = null;
        try {
            // 拼接请求URL
            if (params != null && params.length() > 0) {
                url += "?" + params;
            }
            
            httpGet = new HttpGet(url);

            if (headers != null && headers.size() > 0) {
                for (String key : headers.keySet()) {
                    httpGet.addHeader(key, headers.get(key));
                }
            }
            // 执行
            return httpReturn(client.execute(httpGet));
        } catch (Exception e) {
            if (httpGet != null) {
                httpGet.releaseConnection();
            }
            return null;
        }
    }

    public String httpPost(String url, Map<String, String> paraValues, Charset charset) {
        return httpPost(url, paraValues, charset, null);
    }

    public String httpPost(String url, Map<String, String> paraValues, Charset charset, Map<String, String> headers) {
        HttpPost post = null;
        try {
            if (paraValues == null) {
                return "";
            }
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            Iterator<String> iter = paraValues.keySet().iterator();
            while (iter.hasNext()) {
                String key = iter.next();
                String value = paraValues.get(key);
                params.add(new BasicNameValuePair(key, value));
            }

            post = new HttpPost(url);

            if (headers != null && headers.size() > 0) {
                for (Entry<String, String> entry : headers.entrySet()) {
                    post.addHeader(entry.getKey(), entry.getValue());
                }
            }
            HttpEntity formEntity = new UrlEncodedFormEntity(params, charset);
            post.setEntity(formEntity);

            // 执行
            return httpReturn(client.execute(post));
        } catch (Exception e) {
            if (post != null) {
                post.releaseConnection();
            }
            return null;
        }
    }

    /**
     * POST提交JSON参数
     *
     * @param url
     *            请求地址
     * @param json
     *            参数JSON
     * @return
     */
    public String httpPostJson(String url, String json) {
        if (!url.startsWith("http://")) {
            url = "http://" + url;
        }
        HttpPost post = null;
        try {
            post = new HttpPost(url);
            StringEntity entity = new StringEntity(json, "utf-8");
            entity.setContentEncoding("UTF-8");
            entity.setContentType("application/json");
            post.setEntity(entity);
            return httpReturn(client.execute(post));
        } catch (Exception e) {
            if (post != null) {
                post.releaseConnection();
            }
            return null;
        }
    }

    /**
     * 采用post方式发送请求，获取响应数据
     *
     * @param url
     *            url地址
     * @param paraValues
     *            参数值键值对
     * @return json串
     */
    public String httpPost(String url, Map<String, String> paraValues) {
        return httpPost(url, paraValues, Charset.forName("UTF-8"));
    }

    /**
     * 解析响应
     * 
     * @param httpResponse
     * @return
     * @throws IOException
     */
    private static String httpReturn(CloseableHttpResponse httpResponse) throws IOException {
        String response = null;
        // 判断请求结果
        if (httpResponse != null) {
            int statusCode = httpResponse.getStatusLine().getStatusCode();
            if (statusCode == HttpStatus.SC_OK) {
                // 获取报表数据
                HttpEntity httpEntity = httpResponse.getEntity();
                if (httpEntity != null) {
                    response = EntityUtils.toString(httpEntity, ENCODEING);
                    httpEntity.getContent().close();
                }
            } else {
                return "返回码：" + statusCode;
            }
        }
        return response;
    }

    private static void startIdleConnectionMonitorThread(final PoolingHttpClientConnectionManager connMgr) {
        // 初始化30s后每隔10s清除掉失效或者不活动的连接
        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                // 关闭失效的连接
                connMgr.closeExpiredConnections();
                // 可选的, 关闭30秒内不活动的连接
                connMgr.closeIdleConnections(30, TimeUnit.SECONDS);
            }
        }, 10000, 10000);
    }

}
