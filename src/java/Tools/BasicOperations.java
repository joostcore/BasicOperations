package Tools;

import Tools.exceptions.Different;
import Tools.exceptions.NotFound;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HttpContext;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;
import org.openqa.selenium.Cookie;

import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;

public class BasicOperations {


    int TIME_OUT = 15000;
    public CookieMonster cookieMonster = new CookieMonster();
    HttpClient http = null;
    org.apache.http.client.config.RequestConfig RequestConfig;
    Header[] basicHeaders;
    public Header[] defaultHeaders = new Header[]{

            new BasicHeader("Accept", "*/*"),
            new BasicHeader("Accept-Encoding", "gzip, deflate, br"),
            new BasicHeader("Accept-Language", "de,en-US;q=0.7,en;q=0.3"),

            new BasicHeader("Cache-Control", "no-cache"),
            new BasicHeader("Connection", "keep-alive"),

            new BasicHeader("DNT", "1"),

            new BasicHeader("Pragma", "no-cache"),

            new BasicHeader("Sec-Fetch-Dest", "empty"),
            new BasicHeader("Sec-Fetch-Mode", "no-cors"),
            new BasicHeader("Sec-Fetch-Site", "same-origin"),

            new BasicHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.15; rv:92.0) Gecko/20100101 Firefox/92.0"),

    };

    boolean logResponse = false;

    boolean withProxy = false;

    public BasicOperations() {
        //refreshHeader();
        this.withProxy = false;
        buildHttpClient();

    }

    public BasicOperations(Header[] headers) {
        //refreshHeader();
        basicHeaders = headers;
        buildHttpClient();
    }

    public BasicOperations(Header[] headers, boolean withProxy) {
        //refreshHeader();
        this.withProxy = withProxy;
        buildHttpClient();
    }

    public BasicOperations(boolean withProxy) {
        //refreshHeader();
        this.withProxy = withProxy;
        buildHttpClient();

    }

    public BasicOperations(boolean withProxy, boolean logging) {
        //refreshHeader();
        this.withProxy = withProxy;
        this.logResponse = logging;
        buildHttpClient();

    }

    public BasicOperations(int timeout) {
        //refreshHeader();
        this.TIME_OUT = timeout;
        this.withProxy = false;
        buildHttpClient();

    }
    public void buildHttpClient() {

        HttpRequestRetryHandler retryHandler = new HttpRequestRetryHandler() {
            @Override
            public boolean retryRequest(IOException e, int i, HttpContext httpContext) {
                if (i < 3) {
                    try {
                        Thread.sleep(TIME_OUT);
                    } catch (InterruptedException interruptedException) {
                        interruptedException.printStackTrace();
                    }
                    return true;
                } else {
                    return true;
                }
            }
        };

        Registry<ConnectionSocketFactory> reg = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", PlainConnectionSocketFactory.INSTANCE)
                .register("https", new MyConnectionFactory(SSLContexts.createSystemDefault()))
                .build();

        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager(reg);
        HttpClientBuilder builder = HttpClientBuilder.create();

        if (withProxy) {
            builder.setConnectionManager(cm);
        }


        builder.setDefaultCookieStore(cookieMonster.httpCookieStore);
        builder.setUserAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.15; rv:92.0) Gecko/20100101 Firefox/92.0");
        builder.setRedirectStrategy(new LaxRedirectStrategy());
        builder.setRetryHandler(retryHandler);


        org.apache.http.client.config.RequestConfig.Builder requestBuilder = RequestConfig.custom();

        requestBuilder.setConnectTimeout(TIME_OUT);
        requestBuilder.setConnectionRequestTimeout(TIME_OUT);
        requestBuilder.setSocketTimeout(TIME_OUT);
        RequestConfig = requestBuilder.build();

        http = null;
        http = builder.build();

    }

    public void buildHttpClient(HttpHost proxy) {

        HttpRequestRetryHandler retryHandler = new HttpRequestRetryHandler() {
            @Override
            public boolean retryRequest(IOException e, int i, HttpContext httpContext) {
                if (i < 3) {
                    try {
                        Thread.sleep(TIME_OUT);
                    } catch (InterruptedException interruptedException) {
                        interruptedException.printStackTrace();
                    }
                    return true;
                } else {
                    return false;
                }
            }
        };

        HttpClientBuilder builder = HttpClientBuilder.create();


        builder.setDefaultCookieStore(cookieMonster.httpCookieStore);
        builder.setUserAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.15; rv:92.0) Gecko/20100101 Firefox/92.0");
        builder.setRedirectStrategy(new LaxRedirectStrategy());
        builder.setRetryHandler(retryHandler);

        builder.setProxy(proxy);

        org.apache.http.client.config.RequestConfig.Builder requestBuilder = RequestConfig.custom();

        requestBuilder.setConnectTimeout(TIME_OUT);
        requestBuilder.setConnectionRequestTimeout(TIME_OUT);
        requestBuilder.setSocketTimeout(60000);
        RequestConfig = requestBuilder.build();

        http = null;
        http = builder.build();

    }

    public String basicGET(String url) throws Different, NotFound, IOException {

        HttpGet httpGet = new HttpGet(url);
        httpGet.setConfig(this.RequestConfig);
        httpGet.setHeaders(basicHeaders);

        HttpResponse httpResponse = http.execute(httpGet);

        if (httpResponse.getStatusLine().getStatusCode() == 404) {
            System.err.println(httpResponse.getStatusLine().getStatusCode());
            EntityUtils.consumeQuietly(httpResponse.getEntity());
            throw new NotFound();
        } else if (httpResponse.getStatusLine().getStatusCode() != 200) {
            System.err.println(httpResponse.getStatusLine().getStatusCode());
            EntityUtils.consumeQuietly(httpResponse.getEntity());
            throw new Different();
        } else {

            Header encoding = httpResponse.getEntity().getContentEncoding();
            String return_string = EntityUtils.toString(httpResponse.getEntity(), encoding == null ? "UTF-8" : encoding.getValue());
            EntityUtils.consumeQuietly(httpResponse.getEntity());
            if (this.logResponse) {
                FileWriter fileWriter = new FileWriter("json_responses.txt", true);
                fileWriter.append("\n" + return_string);
                fileWriter.flush();

            }
            return return_string;
        }

    }

    public HttpResponse basicGETWithBody(String url) throws Different, NotFound, IOException {

        HttpGet httpGet = new HttpGet(url);
        httpGet.setConfig(this.RequestConfig);
        httpGet.setHeaders(basicHeaders);

        HttpResponse httpResponse = http.execute(httpGet);

        if (httpResponse.getStatusLine().getStatusCode() == 404) {
            System.err.println(httpResponse.getStatusLine().getStatusCode());
            EntityUtils.consumeQuietly(httpResponse.getEntity());
            throw new NotFound();
        } else if (httpResponse.getStatusLine().getStatusCode() != 200) {
            System.err.println(httpResponse.getStatusLine().getStatusCode());
            EntityUtils.consumeQuietly(httpResponse.getEntity());
            throw new Different();
        } else {

            Header encoding = httpResponse.getEntity().getContentEncoding();
            String return_string = EntityUtils.toString(httpResponse.getEntity(), encoding == null ? "UTF-8" : encoding.getValue());
            EntityUtils.consumeQuietly(httpResponse.getEntity());
            if (this.logResponse) {
                FileWriter fileWriter = new FileWriter("json_responses.txt", true);
                fileWriter.append("\n" + return_string);
                fileWriter.flush();

            }
            return httpResponse;
        }

    }

    public String basicGET(String url, HttpHost proxy) throws Different, NotFound, IOException {
        buildHttpClient(proxy);

        HttpGet httpGet = new HttpGet(url);
        httpGet.setConfig(this.RequestConfig);
        httpGet.setHeaders(basicHeaders);


        HttpResponse httpResponse = http.execute(httpGet);

        if (httpResponse.getStatusLine().getStatusCode() == 404) {
            System.err.println(httpResponse.getStatusLine().getStatusCode());
            EntityUtils.consumeQuietly(httpResponse.getEntity());
            throw new NotFound();
        } else if (httpResponse.getStatusLine().getStatusCode() != 200) {
            System.err.println(httpResponse.getStatusLine().getStatusCode());
            EntityUtils.consumeQuietly(httpResponse.getEntity());
            throw new Different();
        } else {

            Header encoding = httpResponse.getEntity().getContentEncoding();
            String return_string = EntityUtils.toString(httpResponse.getEntity(), encoding == null ? "UTF-8" : encoding.getValue());
            EntityUtils.consumeQuietly(httpResponse.getEntity());
            if (this.logResponse) {
                FileWriter fileWriter = new FileWriter("json_responses.txt", true);
                fileWriter.append("\n" + return_string);
                fileWriter.flush();

            }
            return return_string;
        }

    }


    public String basicPost(String json_payload, String url) throws IOException, NotFound, Different {

        HttpPost httpPost = new HttpPost(url);
        httpPost.setConfig(RequestConfig);
        httpPost.setHeaders(basicHeaders);

        StringEntity entity = new StringEntity(json_payload, ContentType.APPLICATION_JSON);
        httpPost.setEntity(entity);
        HttpResponse httpResponse = http.execute(httpPost);


        if (httpResponse.getStatusLine().getStatusCode() == 404) {
            EntityUtils.consumeQuietly(httpResponse.getEntity());
            throw new NotFound();
        } else if (httpResponse.getStatusLine().getStatusCode() != 200) {
            EntityUtils.consumeQuietly(httpResponse.getEntity());
            throw new Different();
        } else {
            Header encoding = httpResponse.getEntity().getContentEncoding();
            String return_string = EntityUtils.toString(httpResponse.getEntity(), encoding == null ? "UTF-8" : encoding.getValue());
            EntityUtils.consumeQuietly(httpResponse.getEntity());
            if (this.logResponse) {
                FileWriter fileWriter = new FileWriter("json_responses.txt", true);
                fileWriter.append("\n" + return_string);
                fileWriter.flush();

            }
            return return_string;
        }

    }


    public String basicPost(List<NameValuePair> params, String url) throws IOException, NotFound, Different {


        HttpPost httpPost = new HttpPost(url);
        httpPost.setConfig(RequestConfig);
        httpPost.setHeaders(basicHeaders);

        UrlEncodedFormEntity urlEncodedFormEntity = new UrlEncodedFormEntity(params);
        httpPost.setEntity(urlEncodedFormEntity);
        HttpResponse httpResponse = http.execute(httpPost);

        if (httpResponse.getStatusLine().getStatusCode() == 404) {
            EntityUtils.consumeQuietly(httpResponse.getEntity());
            throw new NotFound();
        } else if (httpResponse.getStatusLine().getStatusCode() != 200) {
            EntityUtils.consumeQuietly(httpResponse.getEntity());
            throw new Different();
        } else {
            Header encoding = httpResponse.getEntity().getContentEncoding();
            String return_string = EntityUtils.toString(httpResponse.getEntity(), encoding == null ? "UTF-8" : encoding.getValue());
            EntityUtils.consumeQuietly(httpResponse.getEntity());
            if (this.logResponse) {
                FileWriter fileWriter = new FileWriter("json_responses.txt", true);
                fileWriter.append("\n" + return_string);
                fileWriter.flush();

            }
            if (return_string.isEmpty()) {
                System.out.println(httpResponse.getStatusLine().getStatusCode() + " " + httpResponse.getStatusLine().getReasonPhrase());
            }
            return return_string;
        }

    }

    public void setSeleniumCookies(Collection<Cookie> cookies) {

        for (Cookie cookie : cookies) {
            BasicClientCookie basicClientCookie = new BasicClientCookie(cookie.getName(), cookie.getValue());
            basicClientCookie.setDomain(cookie.getDomain());
            basicClientCookie.setPath(cookie.getPath());
            basicClientCookie.setExpiryDate(cookie.getExpiry());
            this.cookieMonster.httpCookieStore.addCookie(basicClientCookie);
        }


    }


}
