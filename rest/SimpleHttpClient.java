package com.hackeruso.automation.model.api.rest;

import com.hackeruso.automation.conf.EnvConf;
import com.hackeruso.automation.utils.Waiter;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.awaitility.Duration;
import org.awaitility.core.Condition;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import static com.hackeruso.automation.logger.LoggerFactory.Log;

public class SimpleHttpClient {
    private static final long REQUEST_TIMEOUT = 60L;
    private static final long RESPONSE_TIMEOUT = 60L;
    public static final MediaType JSON_MEDIA_TYPE = MediaType.parse(EnvConf.getProperty("application.json"));
    public static final MediaType URL_ENCODE_MEDIA_TYPE = MediaType.parse(EnvConf.getProperty("application.urlencoded"));
    public final Duration timeout;
    private OkHttpClient client = new OkHttpClient();
    private final AtomicReference<IOException> exception = new AtomicReference<>();
    private final String host;

    private SimpleHttpClient(String host){
        this.host = host;
        setConnectionTimeout(REQUEST_TIMEOUT);
        setResponseTimeout();
        timeout = new Duration(5L * (client.readTimeoutMillis() + client.connectTimeoutMillis()), TimeUnit.MILLISECONDS);
    }

    private void setResponseTimeout() {
        client = new OkHttpClient.Builder().readTimeout(SimpleHttpClient.RESPONSE_TIMEOUT, TimeUnit.SECONDS).build();
    }

    public static SimpleHttpClient create(String host) {
        return new SimpleHttpClient(host);
    }

    public Response sendWithRetry(final Request request) throws IOException {
        Condition<Response> condition = () -> {
            try{
                exception.set(null);
                return send(request);
            }catch(IOException e){
                Log.d(e , "failed to sent request=[%s] to host=[%s]" , request , host);
                exception.set(e);
                return null;
            }
        };
        Response response = Waiter.waitCondition(timeout, condition, Duration.ONE_SECOND);
        checkResponse(response);
        return response;
    }

    public void setConnectionTimeout(Long timeout){
        client = new OkHttpClient.Builder().connectTimeout(timeout, TimeUnit.SECONDS).build();
    }

    public Response send(Request request) throws IOException {
        return client.newCall(request).execute();
    }

    private void checkResponse(Response response) throws IOException {
        if(exception.get() !=null){
            throw exception.get();
        }else if(response == null){
            throw new IOException(String.format("failed to send request to host=[%s] after timeout=[%s]", host, REQUEST_TIMEOUT));
        }
    }
}
