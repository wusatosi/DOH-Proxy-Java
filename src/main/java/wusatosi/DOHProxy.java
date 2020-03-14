package wusatosi;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;

public class DOHProxy {

    private static String remoteAddress = getUpstream();

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

    private static byte[] readALLBytes(InputStream inputStream) throws IOException {
        int available = inputStream.available();
        byte[] result = new byte[available];
        //noinspection ResultOfMethodCallIgnored
        inputStream.read(result);
        inputStream.close();
        return result;
    }

}
