package wusatosi;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;

public class DOHProxy {

    private static String upstream = getUpstream();

    private static String getUpstream() {
        String upstream = System.getenv("upstream");
        if (upstream != null) {
            upstream = System.getProperty("upstream");

            if (upstream != null) {
                upstream = "https://dns.rubyfish.cn/dns-query";
            }
        }
        return upstream;
    }

    public static byte[] makeRequest(byte[] raw) throws IOException {
        HttpURLConnection httpClient = setupHttpURLConnection(raw);
        try {
            System.out.printf("received status code:%d, %s%n", httpClient.getResponseCode(), httpClient.getResponseMessage());
        } catch (IOException e) {
            logIOExceptionDuringRequest(httpClient, e);
            throw e;
        }
        InputStream inputStream = httpClient.getInputStream();
        return readALLBytes(inputStream);
    }

    private static HttpURLConnection setupHttpURLConnection(byte[] raw) throws IOException {
        HttpURLConnection httpClient = (HttpURLConnection) new URL(upstream).openConnection(Proxy.NO_PROXY);
        httpClient.setRequestMethod("POST");
        setupAcceptAndContentType(httpClient);
        writeRequest(httpClient, raw);
        return httpClient;
    }

    private static void setupAcceptAndContentType(HttpURLConnection httpClient) {
        httpClient.setRequestProperty("accept", "application/dns-message");
        httpClient.setRequestProperty("content-type", "application/dns-message");
    }

    private static void writeRequest(HttpURLConnection httpClient, byte[] raw) throws IOException {
        httpClient.setDoOutput(true);
        OutputStream output = httpClient.getOutputStream();
        output.write(raw);
        output.flush();
        output.close();
    }

    private static void logIOExceptionDuringRequest(HttpURLConnection hcon, IOException e) throws IOException {
        System.out.println("non-normal status code or request failed (" + e + ")");
        System.out.println(new String(readALLBytes(hcon.getErrorStream())));
    }

    private static byte[] readALLBytes(InputStream inputStream) throws IOException {
        int available = inputStream.available();
        byte[] result = new byte[available];
        //noinspection ResultOfMethodCallIgnored
        inputStream.read(result);
        inputStream.close();
        return result;
    }

}
