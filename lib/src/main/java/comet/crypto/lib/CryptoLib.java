package comet.crypto.lib;

import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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
    private final String HASH_ALGORITHM = "SHA-256";
    private int hashLength = 64;
    public int difficulty_target;  // Number of leading zeros in target hash
    private Block block;

    // HTTP client
    private final OkHttpClient httpClient = new OkHttpClient();

    // Constructor
    private CryptoLib() {}

    // Testing
    public String runPrint() {
        return "CryptoLib is running.";
    }

    // HTTP Requests
    public String getNewTask() throws IOException {
        Request request = new Request.Builder()
                .url("https://load-balancer-server.vercel.app/communication/newTask")
                .build();
        Response response = httpClient.newCall(request).execute();
        String json = response.body().string();
        JsonObject jsonObject = new Gson().fromJson(json, JsonObject.class);
        response.body().close();
        this.block = new Block(jsonObject.get("hash").getAsString(),
                jsonObject.get("height").getAsInt(),
                jsonObject.get("previousBlockHash").getAsString(),
                jsonObject.get("timestamp").getAsLong(),
                jsonObject.get("transactionsCount").getAsInt(),
                jsonObject.get("difficultyTarget").getAsInt(),
                jsonObject.get("merkleRoot").getAsString());
        String result = mine();
        return result;
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

    // Mining Functionality

    private class Block {
        String hash;
        int height;
        String previousBlockHash;
        long timestamp;
        int transactionsCount;
        int difficulty;
        String merkleRoot;

        public Block(String hash, int height, String previousBlockHash, long timestamp,
                     int transactionsCount, int difficulty, String merkleRoot) {
            this.hash = hash;
            this.height = height;
            this.previousBlockHash = previousBlockHash;
            this.timestamp = timestamp;
            this.transactionsCount = transactionsCount;
            this.difficulty = difficulty;
            this.merkleRoot = merkleRoot;
        }

    }

    public String mine() {
        // Try different nonces until a valid one is found
        long nonce = 0;
        String hash;
        do {
            nonce++;
            hash = calculateHash(this.block.previousBlockHash,
                    this.block.timestamp,
                    this.block.merkleRoot,
                    nonce,
                    this.block.difficulty);

        } while (!hashMeetsDifficultyTarget(hash));

        return hash;
    }

    private String calculateHash(String previousBlockHash, long timestamp, String merkleRoot, long nonce, int difficultyTarget) {
        String data = previousBlockHash + timestamp + merkleRoot + nonce + difficultyTarget;
        try {
            MessageDigest digest = MessageDigest.getInstance(HASH_ALGORITHM);
            byte[] hash = digest.digest(data.getBytes());
            return String.format("%064x", new BigInteger(1, hash));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean hashMeetsDifficultyTarget(String hash) {
        for (int i = 0; i < difficulty_target; i++) {
            if (hash.charAt(i) != '0') {
                return false;
            }
        }
        return true;
    }

}