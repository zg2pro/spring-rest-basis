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
package com.github.zg2pro.spring.rest.basis.template;

import com.fasterxml.jackson.databind.module.SimpleModule;
import com.github.zg2pro.spring.rest.basis.logs.LoggingRequestFactoryFactory;
import com.github.zg2pro.spring.rest.basis.logs.LoggingRequestInterceptor;
import java.util.ArrayList;
import java.util.List;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.InterceptingClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;

/**
 *
 * Layer over spring's RestTemplate loading automatically the lib utilities
 *
 * @author zg2pro
 */
public class Zg2proRestTemplate extends AbstractZg2proRestTemplate {

    @Override
    protected void interceptorsIntegration(List<ClientHttpRequestInterceptor> lInterceptors, Object sslConfiguration) {
        this.setInterceptors(lInterceptors);
        SimpleClientHttpRequestFactory chrf = new SimpleClientHttpRequestFactory();
        chrf.setOutputStreaming(false);
        this.setRequestFactory(
                new InterceptingClientHttpRequestFactory(
                        new BufferingClientHttpRequestFactory(chrf),
                        lInterceptors
                )
        );
    }
    
    /**
     * a RestTemplate including logging interceptor The constructor also
     * initializes the RestTemplateErrorHandler, and jackson is initialized with
     * a simple ObjectMapper containing a camelCaseToKebabCase policy.
     *
     * Also it loads a FormHttpMessageConverter, a StringHttpMessageConverter,
     * a, ResourceHttpMessageConverter, and a ByteArrayHttpMessageConverter, of
     * course at build you should already have loaded your json converter
     */
    public Zg2proRestTemplate() {
        this(null);
    }

    /**
     * a RestTemplate including logging interceptor The constructor also
     * initializes the RestTemplateErrorHandler, and jackson is initialized
     * thanks to the simplemodule.
     *
     * Also it loads a FormHttpMessageConverter, a StringHttpMessageConverter,
     * a, ResourceHttpMessageConverter, and a ByteArrayHttpMessageConverter, of
     * course at build you should already have loaded your json converter
     *
     * @param sm simple module from jackson
     */
    public Zg2proRestTemplate(SimpleModule sm) {
        super(sm);
        //interceptors
        LoggingRequestInterceptor lri = new LoggingRequestInterceptor();
        this.setInterceptors(new ArrayList<>());
        this.getInterceptors().add(lri);
        this.setRequestFactory(LoggingRequestFactoryFactory.build(lri));
    }

    /**
     * a RestTemplate including your arguments for message converters and
     * interceptors. The constructor also initializes the
     * RestTemplateErrorHandler.
     *
     * @param lConverters - among which could jackson customized with the
     * CamelCaseToKebabCase policy
     * @param lInterceptors - among which could be LoggingRequestInterceptor
     */
    public Zg2proRestTemplate(
            List<HttpMessageConverter<?>> lConverters, 
            List<ClientHttpRequestInterceptor> lInterceptors) {
        super(lConverters, lInterceptors);
        interceptorsIntegration(lInterceptors, null);
    }

}
