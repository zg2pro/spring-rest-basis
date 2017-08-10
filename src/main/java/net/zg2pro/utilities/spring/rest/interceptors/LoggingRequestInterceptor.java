package net.zg2pro.utilities.spring.rest.interceptors;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import org.springframework.http.client.ClientHttpRequestInterceptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;
import static org.slf4j.event.Level.DEBUG;
import static org.slf4j.event.Level.ERROR;
import static org.slf4j.event.Level.INFO;
import static org.slf4j.event.Level.TRACE;
import static org.slf4j.event.Level.WARN;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.StringUtils;

/**
 *
 * Use this class as an interceptor to log every request and response used by
 * your RestTemplate
 *
 * @author zg2pro
 */
public class LoggingRequestInterceptor implements ClientHttpRequestInterceptor {

    private static final int BUFFER_LENGTH = 16;
    private static final int MARK_LENGTH = 24 * BUFFER_LENGTH;
    private static final int DEFAULT_BODY_LENGTH = 10000;
    private static final Charset DEFAULT_ENCODING = StandardCharsets.UTF_8;
    private static final Level DEFAULT_LEVEL = Level.DEBUG;

    private final static Logger logger = LoggerFactory.getLogger(LoggingRequestInterceptor.class);
    private final static Level loggerLevel = loggerLevel();

    private final Charset encoding;
    private final int maxBodyLength;
    private final Level lriLevel;

    private static Level loggerLevel() {
        if (logger.isTraceEnabled()) {
            return TRACE;
        } else if (logger.isDebugEnabled()) {
            return DEBUG;
        } else if (logger.isInfoEnabled()) {
            return INFO;
        } else if (logger.isWarnEnabled()) {
            return WARN;
        } else {
            return ERROR;
        }
    }

    private void log(String txt, Object... args) {
        switch (lriLevel) {
            case TRACE:
                logger.trace(txt, args);
                break;
            case DEBUG:
                logger.debug(txt, args);
                break;
            case INFO:
                logger.info(txt, args);
                break;
            case WARN:
                logger.warn(txt, args);
                break;
            default:
                logger.error(txt, args);
        }
    }

    /**
     * default encoding to trace your http calls is UTF-8, it also uses a max
     * body length in response or request equal to 10000 characters, as well as
     * a DEBUG log lriLevel, to add an interceptor to a RestTemplate, use
     * addInterceptors() method
     */
    public LoggingRequestInterceptor() {
        super();
        this.encoding = DEFAULT_ENCODING;
        this.maxBodyLength = DEFAULT_BODY_LENGTH;
        this.lriLevel = DEFAULT_LEVEL;
    }

    /**
     * use this constructor to build the interceptor with custom values in
     * encoding and maxBodyLength, as well as an slf4j lriLevel, to add an
     * interceptor to a RestTemplate, use addInterceptors() method
     *
     * @param encoding
     * @param maxBodyLength
     */
    public LoggingRequestInterceptor(Charset encoding, int maxBodyLength, Level level) {
        super();
        if (maxBodyLength < 1) {
            throw new IllegalArgumentException("please set a limit to the body length writable in your logs");
        }
        this.encoding = encoding;
        this.maxBodyLength = maxBodyLength;
        this.lriLevel = level;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        boolean mustLog = loggerLevel.compareTo(lriLevel) > -1;
        if (mustLog) {
            traceRequest(request, body);
        }
        ClientHttpResponse response = execution.execute(request, body);
        if (mustLog) {
            traceResponse(response);
        }
        return response;
    }

    private void traceRequest(HttpRequest request, byte[] body) throws IOException {
        log("===========================request begin================================================");
        log("URI : {}", request.getURI());
        log("Method : {}", request.getMethod());
        if (body.length < maxBodyLength) {
            log("Request Body : {}", new String(body, encoding));
        }
        log("==========================request end================================================");
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
            log("============ClientHttpResponse body null====================" + ioe);
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }

    private void logResponse(ClientHttpResponse response, final BufferedReader bufferedReader, char[] buffer) throws IOException {
        log("============================response begin==========================================");
        log("status code: {}", response.getStatusCode());
        log("status text: {}", response.getStatusText());
        if (bufferedReader.markSupported()) {
            writeBody(bufferedReader, buffer);
        }
        bufferedReader.close();
        log("=======================response end=================================================");
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
        log(inputStringBuilder.toString());
    }

}
