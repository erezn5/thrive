package com.hackeruso.automation.model.api;

import com.google.gson.Gson;
import com.hackeruso.automation.model.api.rest.SimpleHttpClient;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static com.hackeruso.automation.logger.LoggerFactory.Log;

public abstract class ServiceApiCommon<R> {
    private final Map<String , String> headers = new HashMap<>();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    protected static final Gson GSON = new Gson();
    protected SimpleHttpClient client;
    protected String baseUrl;
    private  String token;

    protected ServiceApiCommon(String host, String token){
        commonSetup(host);
        this.token = token;
        addAuthorizationHeader(token);
    }

    protected ServiceApiCommon(String host){
        commonSetup(host);
    }

    private void commonSetup(String host){
        client = SimpleHttpClient.create(host);
        baseUrl = host;
        addContentTypeHeader();
        addAcceptJsonHeader();
    }

    protected abstract R deserializeResponse(String bodyStr);

    protected Future<R> postJsonAsync(String path , String jsonBody) {
        return postWithAsync(path , jsonBody , SimpleHttpClient.JSON_MEDIA_TYPE);
    }

    private Future<R> postWithAsync(String path , String jsonBody , MediaType mediaType) {
        Request.Builder requestBuilder = buildPostRequest(path , jsonBody , mediaType);
        return sendAsync(requestBuilder);
    }
    private Future<R> sendAsync(Request.Builder requestBuilder){
        Callable<R> callable = ()-> {
            Response response = client.sendWithRetry(requestBuilder.build());
            return processResponse(response);
        };
        return executor.submit(callable);
    }

    protected R postJson(String path, String jsonBody) throws IOException {
        return sendWithRetry(path, jsonBody, SimpleHttpClient.JSON_MEDIA_TYPE);
    }

    public R ping() throws IOException {
        return get("");
    }

    protected R get(String pathAndQuery) throws IOException {
        Request.Builder requestBuilder = new Request.Builder()
                .url(baseUrl + pathAndQuery)
                .get();
        addAllHeaders(requestBuilder);
        Response response = client.sendWithRetry(requestBuilder.build());
        return processResponse(response);
    }

    private R sendWithRetry(String path, String jsonBody, MediaType mediaType) throws IOException {
        Request.Builder requestBuilder = buildPostRequest(path, jsonBody, mediaType);
        Response response = sendWithRetry(requestBuilder);
        return processResponse(response);
    }

    protected void addAuthorizationHeader(String token){
        addHeader("Authorization", token);
        Log.i("Token is=[%s]", token);
    }

    protected void addAcceptJsonHeader(){
        addHeader("Accept", "application/json");
    }

    protected void addContentTypeHeader(){
        addHeader("Content-Type", "application/json");
    }

    protected Response sendWithRetry(Request.Builder requestBuilder) throws IOException {
        return client.sendWithRetry(requestBuilder.build());
    }

    private Request.Builder buildPostRequest( String path ,String jsonBody , MediaType mediaType) {
        Request.Builder requestBuilder = new Request.Builder()
                .url(baseUrl + path)
                .post(RequestBody.create(mediaType, jsonBody));
        addAllHeaders(requestBuilder);
        return requestBuilder;
    }

    private R processResponse(Response response) throws IOException {
        String bodyStr = validateOk(response);
        System.out.println("response body=[" + bodyStr + "]");
        return deserializeResponse(bodyStr);
    }

    protected void addHeader(final String name , final String value){
        headers.put(name , value);
    }

    private void addAllHeaders(Request.Builder builder){
        for(Map.Entry<String , String> entry : headers.entrySet()){
            builder.header(entry.getKey() , entry.getValue());
        }
    }

    protected static String validateOk(Response response) throws IOException {
        String bodyStr = Objects.requireNonNull(response.body()).string();
        if(response.isSuccessful()){
            return bodyStr;
        }else{
            Log.e("error response body=[%s]" , bodyStr);
            throw new IOException("request=[" + response.request().url() + "] return with status code=["
                    + response.code() + "], reason=[" + bodyStr + "]");
        }
    }

    protected static <RES> RES deserializeResponse(String bodyStr, Class<RES> modelClass) {
        return GSON.fromJson(bodyStr, modelClass);
    }
}
