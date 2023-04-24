package comet.crypto.lib;

import java.io.IOException;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class CryptoLib {

    // Instance
    private static final CryptoLib _instance = new CryptoLib();
    public static CryptoLib instance() {
        return _instance;
    }

    // HTTP client
    private final OkHttpClient httpClient = new OkHttpClient();

    // Constructor
    private CryptoLib() {}

    // Testing
    public String runPrint() {
        return "CryptoLib is running.";
    }

    public JsonObject getNewTask() throws IOException {
        Request request = new Request.Builder()
                .url("https://load-balancer-server.vercel.app/communication/newTask")
                .build();
        Response response = httpClient.newCall(request).execute();
        String json = response.body().string();
        JsonObject jsonObject = new Gson().fromJson(json, JsonObject.class);
        response.body().close();
        return jsonObject;
    }

    public void sendTaskResult(String result) throws IOException {
        Gson gson = new Gson();
        JsonObject data = new JsonObject();
        data.addProperty("hash",result);
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, gson.toJson(data));
        Request request = new Request.Builder()
                .url("https://load-balancer-server.vercel.app/communication/taskFinished")
                .post(body)
                .build();
        Response response = httpClient.newCall(request).execute();
        response.body().close();
    }
}