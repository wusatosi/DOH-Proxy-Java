package wusatosi;

import com.qcloud.services.scf.runtime.events.APIGatewayProxyRequestEvent;
import com.qcloud.services.scf.runtime.events.APIGatewayProxyResponseEvent;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.util.Base64;
import java.util.HashMap;

public class DOHProxy {

    private static String remoteAddress = "https://dns.rubyfish.cn/dns-query";

    public String handle(APIGatewayProxyRequestEvent event) throws IOException {
        System.out.println("version 1.3.2");
//        // no point to process exception, client won't understand anyway
//        byte[] bytes = event.getBody().getBytes();
//        System.out.println("raw request: " + Base64.getEncoder().encodeToString(bytes));
//        byte[] response = makeRequest(bytes);
        String base = "AACBgAABAAEAAAABBmdvb2dsZQNjb20AABwAAQZnb29nbGUDY29tAAAcAAEAAAD5ABAkBGgAQAUICQAAAAAAACAOAAApEAAAAAAAAAsACAAHAAEYAHychQ==";
        return new String(Base64.getDecoder().decode(base));
    }


    static byte[] makeRequest(byte[] raw) throws IOException {
        HttpURLConnection hcon = (HttpURLConnection) new URL(remoteAddress).openConnection(Proxy.NO_PROXY);
        hcon.setRequestMethod("POST");
        hcon.setRequestProperty("accept", "application/dns-message");
        hcon.setRequestProperty("content-type", "application/dns-message");
        hcon.setDoOutput(true);
        OutputStream output = hcon.getOutputStream();
        output.write(raw);
        output.flush();
        output.close();
        try {
            System.out.printf("received status code:%d, %s%n", hcon.getResponseCode(), hcon.getResponseMessage());
        } catch (IOException e) {
            System.out.println("non-normal status code or request failed (" + e + ")");
            System.out.println(new String(readALLBytes(hcon.getErrorStream())));
            throw e;
        }
        InputStream inputStream = hcon.getInputStream();
        return readALLBytes(inputStream);
    }

    private static String encodeDohQuestion(byte[] bytes) {
        String encoded = Base64.getEncoder().encodeToString(bytes);
        if (encoded.endsWith("=")) {
            return encoded.substring(0, encoded.length() - 1);
        } else {
            return encoded;
        }
    }

    private static byte[] readALLBytes(InputStream inputStream) throws IOException {
        int available = inputStream.available();
        byte[] result = new byte[available];
        //noinspection ResultOfMethodCallIgnored
        inputStream.read(result);
        inputStream.close();
        return result;
    }

    private APIGatewayProxyResponseEvent makeResponseFromBody(byte[] rawBody) {
        APIGatewayProxyResponseEvent responseEvent = new APIGatewayProxyResponseEvent();
        configResponseBody(responseEvent, rawBody);
        responseEvent.setStatusCode(200);
        configResponseHeaders(responseEvent);
        return responseEvent;
    }

    private void configResponseHeaders(APIGatewayProxyResponseEvent responseEvent) {
        HashMap<String, String> map = new HashMap<>();
        map.put("accept", "application/dns-message");
        map.put("content-type", "application/dns-message");
        map.put("x-upstream", "doh://" + remoteAddress);
        responseEvent.setHeaders(map);
    }

    private void configResponseBody(APIGatewayProxyResponseEvent responseEvent, byte[] rawBody) {
        String base = "AACBgAABAAEAAAABBmdvb2dsZQNjb20AABwAAQZnb29nbGUDY29tAAAcAAEAAAD5ABAkBGgAQAUICQAAAAAAACAOAAApEAAAAAAAAAsACAAHAAEYAHychQ==";
        responseEvent.setBody(new String(Base64.getDecoder().decode(base)));
//        responseEvent.setBody(Base64.getEncoder().encodeToString(rawBody));
        responseEvent.setBody(base);
        responseEvent.setIsBase64Encoded(true);
    }

}
