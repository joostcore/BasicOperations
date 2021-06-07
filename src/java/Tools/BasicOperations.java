package Tools;

import Tools.exceptions.Different;
import Tools.exceptions.NotFound;
import org.apache.http.Header;
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
import org.apache.http.protocol.HttpContext;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;

import java.io.*;
import java.util.List;

public class BasicOperations {


    final int TIME_OUT = 15000;
    public CookieMonster cookieMonster = new CookieMonster();
    HttpClient http = null;
    org.apache.http.client.config.RequestConfig RequestConfig;
    Header[] basicHeaders;

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
                    return false;
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
        builder.setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:63.0) Gecko/20100101 Firefox/63.0");
        builder.setRedirectStrategy(new LaxRedirectStrategy());
        builder.setRetryHandler(retryHandler);


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
            EntityUtils.consumeQuietly(httpResponse.getEntity());
            throw new NotFound();
        } else if (httpResponse.getStatusLine().getStatusCode() != 200) {
            EntityUtils.consumeQuietly(httpResponse.getEntity());
            throw new Different();
        } else {

            String return_string = InputStreamToStringAndClose(httpResponse.getEntity().getContent());
            EntityUtils.consumeQuietly(httpResponse.getEntity());
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
            String return_string = InputStreamToStringAndClose(httpResponse.getEntity().getContent());
            EntityUtils.consumeQuietly(httpResponse.getEntity());
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


        FileWriter fileWriter = new FileWriter("json_responses.txt", true);
        //fileWriter.append("\n"+return_string);

        try {
            Thread.sleep((int) ((Math.random() * 1000) + 1));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        if (httpResponse.getStatusLine().getStatusCode() == 404) {
            EntityUtils.consumeQuietly(httpResponse.getEntity());
            throw new NotFound();
        } else if (httpResponse.getStatusLine().getStatusCode() != 200) {
            EntityUtils.consumeQuietly(httpResponse.getEntity());
            throw new Different();
        } else {
            String return_string = InputStreamToStringAndClose(httpResponse.getEntity().getContent());
            EntityUtils.consumeQuietly(httpResponse.getEntity());
            return return_string;
        }

    }


    public String InputStreamToStringAndClose(InputStream in) throws IOException {
        try {
            StringBuilder stringBuilder = new StringBuilder();
            Reader reader = new InputStreamReader(in);
            int data = reader.read();
            while (data != -1) {
                stringBuilder.append((char) data);
                data = reader.read();
            }
            return stringBuilder.toString();
        } catch (IOException ex) {
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            throw ex;
        }
    }
}
