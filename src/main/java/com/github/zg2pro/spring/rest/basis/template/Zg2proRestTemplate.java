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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.zg2pro.spring.rest.basis.exceptions.RestTemplateErrorHandler;
import com.github.zg2pro.spring.rest.basis.logs.LoggingRequestFactoryFactory;
import java.util.ArrayList;
import java.util.List;
import com.github.zg2pro.spring.rest.basis.serialization.CamelCaseToKebabCaseNamingStrategy;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.InterceptingClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.lang.Nullable;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

/**
 *
 * Layer over spring's RestTemplate loading automatically the lib utilities
 *
 * @author zg2pro
 */
public class Zg2proRestTemplate extends RestTemplate {

    private ObjectMapper camelToKebabObjectMapper() {
        ObjectMapper jsonMapper = new ObjectMapper();
        jsonMapper.setPropertyNamingStrategy(new CamelCaseToKebabCaseNamingStrategy());
        return jsonMapper;
    }

    /**
     * a RestTemplate including logging interceptor The constructor also
     * initializes the RestTemplateErrorHandler.
     */
    public Zg2proRestTemplate() {
        super();
        //converters
        List<HttpMessageConverter<?>> messageConverters = new ArrayList<>();
        ObjectMapper jsonMapper = camelToKebabObjectMapper();
        MappingJackson2HttpMessageConverter jackson2Http = new MappingJackson2HttpMessageConverter(jsonMapper);
        messageConverters.add(0, jackson2Http);
        setMessageConverters(messageConverters);
        //interceptors
        this.setRequestFactory(LoggingRequestFactoryFactory.build());
        //errors handling
        setErrorHandler(new RestTemplateErrorHandler());
    }

    private void interceptorsIntegration(List<ClientHttpRequestInterceptor> lInterceptors) {
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
     * a RestTemplate including your arguments for message converters and
     * interceptors. The constructor also initializes the
     * RestTemplateErrorHandler.
     *
     * @param lConverters - among which could jackson customized with the
     * CamelCaseToKebabCase policy
     * @param lInterceptors - among which could be LoggingRequestInterceptor
     */
    public Zg2proRestTemplate(@Nullable List<HttpMessageConverter<?>> lConverters,
            @Nullable List<ClientHttpRequestInterceptor> lInterceptors) {
        super();
        setErrorHandler(new RestTemplateErrorHandler());
        if (!CollectionUtils.isEmpty(lConverters)) {
            //emptiness is rechecked inside setMessageConverters but it may change 
            //in a future spring release
            setMessageConverters(lConverters);
        }
        if (!CollectionUtils.isEmpty(lInterceptors)) {
            interceptorsIntegration(lInterceptors);
        }
    }
}
