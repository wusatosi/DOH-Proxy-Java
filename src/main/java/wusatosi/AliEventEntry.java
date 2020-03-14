package wusatosi;

import com.aliyun.fc.runtime.Context;
import com.aliyun.fc.runtime.HttpRequestHandler;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;

// invoke by ali-fc service
@SuppressWarnings("unused")
public class AliEventEntry implements HttpRequestHandler {

    @Override
    public void handleRequest(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Context context) throws IOException {
        byte[] request;
        try {
            request = getRequest(httpServletRequest);
            byte[] response = DOHProxy.makeRequest(request);
            relayResponse(httpServletResponse, response);
        } catch (IOException e) {
            relayIOException(e, httpServletResponse);
        } catch (IllegalArgumentException ignored) {
            relayArgumentError(httpServletResponse);
        }
    }

    private void relayResponse(HttpServletResponse httpServletResponse, byte[] response) throws IOException {
        httpServletResponse.setStatus(200);
        ServletOutputStream outputStream = httpServletResponse.getOutputStream();
        outputStream.write(response);
        outputStream.flush();
        outputStream.close();
    }

    private void relayIOException(IOException e, HttpServletResponse httpServletResponse) throws IOException {
        System.out.printf("IOException when proxy: %s%n", e);
        httpServletResponse.sendError(500);
    }

    private void relayArgumentError(HttpServletResponse httpServletResponse) throws IOException {
        httpServletResponse.sendError(400);
    }

    private byte[] getRequest(HttpServletRequest httpServletRequest) {
        try {
            String method = httpServletRequest.getMethod().toLowerCase();
            if (method.equals("post"))
                return readALLBytes(httpServletRequest.getInputStream());
            if (method.equals("get"))
                return Base64.getDecoder().decode(httpServletRequest.getParameter("dns"));
        } catch (IOException e) {
            System.out.printf("parse request failed %s%n", e);
        }
        throw new IllegalArgumentException();
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
