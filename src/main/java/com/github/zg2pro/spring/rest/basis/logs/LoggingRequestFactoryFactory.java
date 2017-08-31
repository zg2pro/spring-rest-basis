/*
 * The MIT License
 *
 * Copyright 2017 zg2pro.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.github.zg2pro.spring.rest.basis.logs;

import java.util.ArrayList;
import java.util.List;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

/**
 *
 * This intercepting request factory integrates LoggingRequestInterceptor so all
 * rest requests and responses will be logged (as long as you set your log
 * levels correctly)
 *
 * @author zg2pro
 * @since 0.2
 */
public class LoggingRequestFactoryFactory {

    public static LoggingRequestFactory build() {
        LoggingRequestInterceptor lri = new LoggingRequestInterceptor();
        return interceptorToRequestFactory(lri);
    }

    public static LoggingRequestFactory build(LoggingRequestInterceptor lri) {
        return interceptorToRequestFactory(lri);
    }

    private static LoggingRequestFactory interceptorToRequestFactory(LoggingRequestInterceptor lri) {
        List<ClientHttpRequestInterceptor> lInterceptors = new ArrayList<>();
        lInterceptors.add(lri);
        SimpleClientHttpRequestFactory chrf = new SimpleClientHttpRequestFactory();
        chrf.setOutputStreaming(false);
        return new LoggingRequestFactory(
                new BufferingClientHttpRequestFactory(chrf),
                lInterceptors
        );
    }
}
