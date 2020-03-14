package wusatosi;

import com.aliyun.fc.runtime.Context;
import com.aliyun.fc.runtime.HttpRequestHandler;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;

public class AliEventEntry implements HttpRequestHandler {

    @Override
    public void handleRequest(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Context context) throws IOException, ServletException {
        byte[] request = readALLBytes(httpServletRequest.getInputStream());
        try {
            byte[] response = DOHProxy.makeRequest(request);
            httpServletResponse.setStatus(200);
            ServletOutputStream outputStream = httpServletResponse.getOutputStream();
            outputStream.write(response);
            outputStream.flush();
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
            httpServletResponse.setStatus(500);
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
}
