package comet.crypto.lib;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

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
        // version + previousBlockHash + merkleRoot + timestamp + bits + nonce
        this.block = new Block(jsonObject.get("version").getAsInt(),
                jsonObject.get("previousBlockHash").getAsString(),
                jsonObject.get("merkleRoot").getAsString(),
                jsonObject.get("timestamp").getAsLong(),
                jsonObject.get("bits").getAsString(),
                jsonObject.get("range").getAsLong());
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
        int version;
        String previousBlockHash;
        String merkleRoot;
        long timestamp;
        String bits;
        long range;

        // version + previousBlockHash + merkleRoot + timestamp + bits + nonce
        public Block(int version, String previousBlockHash, String merkleRoot, long timestamp,
                     String bits, long range) {
            this.version = version;
            this.previousBlockHash = previousBlockHash;
            this.merkleRoot = merkleRoot;
            this.timestamp = timestamp;
            this.bits = bits;
            this.range = range;
        }
    }

    public String mine() {
        // Try different nonces until a valid one is found
        long nonce = 0;
        String hash;
        do {
            nonce++;
            // version + previousBlockHash + merkleRoot + timestamp + bits + nonce
            hash = calculateHash(this.block.version,this.block.previousBlockHash,
                    this.block.merkleRoot,
                    this.block.timestamp,
                    this.block.bits,
                    nonce);

        } while (!hashMeetsDifficultyTarget(hash));

        return hash;
    }

    private String calculateHash(int version, String previousBlockHash, String merkleRoot, long timestamp,
                                 String bits, long nonce) {
        String data = toLittleEndianHex(version) + toLittleEndianHex(previousBlockHash) +
                toLittleEndianHex(merkleRoot) + toLittleEndianHex(Integer.parseInt(String.valueOf(timestamp))) +
                toLittleEndianHex(Integer.parseInt(bits)) + toLittleEndianHex(Integer.parseInt(String.valueOf(nonce)));
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
        for (int i = 0; i < difficulty_target; i++) {
            if (hash.charAt(i) != '0') {
                return false;
            }
        }
        return true;
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