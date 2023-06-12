package comet.crypto.lib;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.FormBody;
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
    private String API_KEY;
    private Request request;
    private final long RANGE_SIZE = 1000000;
    private volatile boolean currentlyMining = false;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private String hashAnswer; // for Testing Demo mode only ! for real answer there is
    // a need for the block header, use it once you have a real blockchain on server

    // HTTP client
    private final OkHttpClient httpClient = new OkHttpClient();

    // Constructor
    private CryptoLib() {}

    // Testing
    public String runPrint() {
        return "CryptoLib is running.";
    }

    public void run(String API_KEY){
        this.API_KEY = API_KEY;
        currentlyMining = true;

        executorService.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    while (currentlyMining) {
                        long startTime = System.currentTimeMillis();
                        String result = _instance.getNewTask();
                        _instance.sendTaskResult(result);
                        long endTime = System.currentTimeMillis();
                        long elapsedTime = endTime - startTime;
                        sendMineTime(elapsedTime);
                    }
                } catch (Exception e) {
                    String ex = e.toString();
                    // Handle the exception here, e.g. show an error dialog
                }
            }
        });
    }

    public void stop(){
        currentlyMining = false;
        executorService.shutdown();
    }

    // HTTP Requests
    public String getNewTask() throws IOException {
        /*Request request = new Request.Builder()
                .url("https://load-balancer-server.vercel.app/communication/newTask")
                .build();
        Response response = httpClient.newCall(request).execute();*/
        RequestBody requestBody = new FormBody.Builder()
                .add("key", this.API_KEY)
                .build();

        request = new Request.Builder()
                .url("https://load-balancer-server.vercel.app/communication/newTask")
                .post(requestBody)
                .addHeader("Content-Type", "application/json")
                .build();
        Response response = httpClient.newCall(request).execute();
        String json = response.body().string();
        JsonObject jsonObject = new Gson().fromJson(json, JsonObject.class);
        response.body().close();
        hashAnswer = jsonObject.get("hash").getAsString(); // Demo mode only
        // version + previousBlockHash + merkleRoot + timestamp + bits + nonce
        this.block = new Block(jsonObject.get("version").getAsInt(),
                jsonObject.get("previousBlockHash").getAsString(),
                jsonObject.get("merkleRoot").getAsString(),
                jsonObject.get("timestamp").getAsLong(),
                jsonObject.get("bits").getAsString(),
                jsonObject.get("range").getAsLong(),
                jsonObject.get("difficulty").getAsDouble());
        return mine();
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

    // this function wont exist on a real environment only on the demo
    private void sendMineTime(long timeElapsed) throws IOException {
        JsonObject job = new JsonObject();
        job.addProperty("appKey", API_KEY);
        job.addProperty("workDuration", timeElapsed);

        JsonObject payload = new JsonObject();
        payload.add("job", job);

        Gson gson = new Gson();
        String json = gson.toJson(payload);

        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), json);

        Request request = new Request.Builder()
                .url("https://web-server-chi.vercel.app/statistics/updateStatistics")
                .post(requestBody)
                .addHeader("Content-Type", "application/json")
                .build();

        Response response = httpClient.newCall(request).execute();
        response.body().close();
    }

    // Mining Functionality

    private class Block {
        int version;
        String previousBlockHash;
        String merkleRoot;
        long timestamp;
        String bits;
        long range;
        double difficulty;

        // version + previousBlockHash + merkleRoot + timestamp + bits + nonce
        public Block(int version, String previousBlockHash, String merkleRoot, long timestamp,
                     String bits, long range,double difficulty) {
            this.version = version;
            this.previousBlockHash = previousBlockHash;
            this.merkleRoot = merkleRoot;
            this.timestamp = timestamp;
            this.bits = bits;
            this.range = range;
            this.difficulty = difficulty;
        }
    }

    public String mine() {
        // Try different nonces until a valid one is found
        long nonce = block.range - RANGE_SIZE - 1;
        BigInteger target;
        BigInteger hashInt;
        String hash;
        do {
            nonce++;
            // version + previousBlockHash + merkleRoot + timestamp + bits + nonce
            hash = calculateHash(this.block.version,this.block.previousBlockHash,
                    this.block.merkleRoot,
                    this.block.timestamp,
                    this.block.bits,
                    nonce);

            /*target = BigInteger.valueOf((long) (this.block.difficulty * Math.pow(2, 256)));
            hashInt = new BigInteger(hash, 16);*/
        } while (hashMeetsDifficultyTarget(hash) && nonce < block.range/* && hashInt.compareTo(target) < 0*/ );

        return hash;
    }

    private String calculateHash(int version, String previousBlockHash, String merkleRoot, long timestamp,
                                 String bits, long nonce) {
        String data = toLittleEndianHex(version) + toLittleEndianHex(previousBlockHash) +
                toLittleEndianHex(merkleRoot) + toLittleEndianHex(Integer.parseInt(String.valueOf(timestamp))) +
                toLittleEndianHex(Integer.parseInt(bits)) + toLittleEndianHex(nonce);
        try {
            byte[] dataBytes = hexStringToByteArray(data);
            MessageDigest digest = MessageDigest.getInstance(HASH_ALGORITHM);
            byte[] hash = digest.digest(dataBytes);
            String hashHex = bytesToHex(hash);
            dataBytes = hexStringToByteArray(hashHex);
            hash = digest.digest(dataBytes);
            return toLittleEndianHex(bytesToHex(hash));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean hashMeetsDifficultyTarget(String hash) {
        return hashAnswer.compareTo(hash) < 0;
        /*for (int i = 0; i < difficulty_target; i++) {
            if (hash.charAt(i) != '0') {
                return false;
            }
        }
        return true;*/
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    private String toLittleEndianHex(Object value) {
        String hexString = null;

        if (value instanceof Integer) {
            int intValue = (int) value;
            hexString = String.format("%08x", Integer.reverseBytes(intValue));
        } else if (value instanceof Long) {
            long longValue = (long) value;
            byte[] timestampBytes = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN).putLong(longValue).array();
            BigInteger timestampBigInt = new BigInteger(1, timestampBytes);
            hexString = timestampBigInt.toString(16);
            if (hexString.length() < 8) {
                hexString = String.format("%08x", Long.reverseBytes(longValue));
            }
            hexString = hexString.substring(0, 8);
        } else if (value instanceof String) {
            String stringValue = (String) value;
            byte[] bytes = hexStringToByteArray(stringValue);
            reverse(bytes);
            hexString = byteArrayToHex(bytes);
        }

        return hexString;
    }

    private byte[] hexStringToByteArray(String hexString) {
        int length = hexString.length();
        byte[] bytes = new byte[length / 2];
        for (int i = 0; i < length - 1; i += 2) {
            bytes[i / 2] = (byte) ((Character.digit(hexString.charAt(i), 16) << 4)
                    + Character.digit(hexString.charAt(i + 1), 16));
        }
        return bytes;
    }

    // Method to convert a byte array to a hexadecimal string
    private String byteArrayToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    private void reverse(byte[] array) {
        if (array == null) {
            return;
        }
        int i = 0;
        int j = array.length - 1;
        byte tmp;
        while (j > i) {
            tmp = array[j];
            array[j] = array[i];
            array[i] = tmp;
            j--;
            i++;
        }
    }
}