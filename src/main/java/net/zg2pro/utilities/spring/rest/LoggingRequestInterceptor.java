package net.zg2pro.utilities.spring.rest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

/**
 *
 * Use this class as an interceptor to log every request and response used by your RestTemplate
 * 
 * @author zg2pro
 */
public class LoggingRequestInterceptor implements ClientHttpRequestInterceptor {

    private static final int BUFFER_LENGTH = 16;
    private static final int MARK_LENGTH = 24 * BUFFER_LENGTH;
    private static final int DEFAULT_BODY_LENGTH = 10000;
    private static final Charset DEFAULT_ENCODING = StandardCharsets.UTF_8;

    private final static Logger logger = LoggerFactory.getLogger(LoggingRequestInterceptor.class);

    private final Charset encoding;
    private final int maxBodyLength;

    /**
     * default encoding to trace your http calls is UTF-8, it also uses a max body length in response or request equal to 10000 characters,
     * to add an interceptor to a RestTemplate, use addInterceptors() method
     */
    public LoggingRequestInterceptor() {
        super();
        this.encoding = DEFAULT_ENCODING;
        this.maxBodyLength = DEFAULT_BODY_LENGTH;
    }

    /**
     * use this constructor to build the interceptor with custom values in encoding and maxBodyLength,
     * to add an interceptor to a RestTemplate, use addInterceptors() method
     * @param encoding
     * @param maxBodyLength
     */
    public LoggingRequestInterceptor(Charset encoding, int maxBodyLength) {
        super();
        this.encoding = encoding;
        this.maxBodyLength = maxBodyLength;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        if (logger.isDebugEnabled()) {
            traceRequest(request, body);
        }
        ClientHttpResponse response = execution.execute(request, body);
        if (logger.isDebugEnabled()) {
            traceResponse(response);
        }
        return response;
    }

    private void traceRequest(HttpRequest request, byte[] body) throws IOException {
        logger.debug("===========================request begin================================================");
        logger.debug("URI : " + request.getURI());
        logger.debug("Method : " + request.getMethod());
        if (body.length < maxBodyLength) {
            logger.debug("Request Body : " + new String(body, encoding));
        }
        logger.debug("==========================request end================================================");
    }

    private void traceResponse(ClientHttpResponse response) throws IOException {
        InputStream is = null;
        try {
            is = response.getBody();
            InputStreamReader isr = null;
            try {
                isr = new InputStreamReader(is, encoding);
                char[] buffer = new char[BUFFER_LENGTH];
                try (BufferedReader bufferedReader = new BufferedReader(isr)) {
                    logResponse(response, bufferedReader, buffer);
                }
            } finally {
                if (isr != null) {
                    isr.close();
                }
            }
        } catch (IOException ioe) {
            logger.debug("============ClientHttpResponse body null====================" + ioe);
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }

    private void logResponse(ClientHttpResponse response, final BufferedReader bufferedReader, char[] buffer) throws IOException {
        logger.debug("============================response begin==========================================");
        logger.debug("status code: " + response.getStatusCode());
        logger.debug("status text: " + response.getStatusText());
        if (bufferedReader.markSupported()) {
            writeBody(bufferedReader, buffer);
        }
        bufferedReader.close();
        logger.debug("=======================response end=================================================");
    }

    private void writeBody(final BufferedReader bufferedReader, char[] buffer) throws IOException {
        StringBuilder inputStringBuilder = new StringBuilder("Response Body : ");
        bufferedReader.mark(MARK_LENGTH);
        int len = bufferedReader.read(buffer);
        String line = new String(buffer);
        int curLen = 0;
        while (!StringUtils.isEmpty(line) && curLen > -1 && len < MARK_LENGTH) {
            inputStringBuilder.append(line);
            curLen = bufferedReader.read(buffer);
            len += curLen;
            line = new String(buffer);
        }
        if (len == MARK_LENGTH) {
            inputStringBuilder.append("...");
        }
        bufferedReader.reset();
        logger.debug(inputStringBuilder.toString());
    }

}
