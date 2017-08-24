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

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.github.zg2pro.spring.rest.basis.exceptions.RestTemplateErrorHandler;
import com.github.zg2pro.spring.rest.basis.logs.LoggingRequestFactoryFactory;
import com.github.zg2pro.spring.rest.basis.logs.LoggingRequestInterceptor;
import java.util.ArrayList;
import java.util.List;
import com.github.zg2pro.spring.rest.basis.serialization.CamelCaseToKebabCaseNamingStrategy;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.InterceptingClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.ResourceHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.lang.Nullable;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MimeTypeUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.ResponseExtractor;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 *
 * Layer over spring's RestTemplate loading automatically the lib utilities
 *
 * @author zg2pro
 */
public class Zg2proRestTemplate extends RestTemplate {

    private List<ClientHttpRequestInterceptor> lInterceptors;

    @Override
    public List<ClientHttpRequestInterceptor> getInterceptors() {
        return lInterceptors;
    }

    @Override
    public void setInterceptors(List<ClientHttpRequestInterceptor> interceptors) {
        this.lInterceptors = interceptors;
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

    private ObjectMapper camelToKebabObjectMapper(SimpleModule sm) {
        ObjectMapper jsonMapper = new ObjectMapper();
        jsonMapper.setPropertyNamingStrategy(new CamelCaseToKebabCaseNamingStrategy());
        if (sm != null) {
            jsonMapper.registerModule(sm);
        }
        jsonMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return jsonMapper;
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
     * @param sm
     */
    public Zg2proRestTemplate(SimpleModule sm) {
        super();
        //converters
        List<HttpMessageConverter<?>> messageConverters = new ArrayList<>();
        messageConverters.add(new FormHttpMessageConverter());
        messageConverters.add(new StringHttpMessageConverter());
        messageConverters.add(new ByteArrayHttpMessageConverter());
        messageConverters.add(new ResourceHttpMessageConverter());
        ObjectMapper jsonMapper = camelToKebabObjectMapper(sm);
        messageConverters.add(new MappingJackson2HttpMessageConverter(jsonMapper));
        this.setMessageConverters(messageConverters);
        //interceptors
        LoggingRequestInterceptor lri = new LoggingRequestInterceptor();
        this.setInterceptors(new ArrayList<>());
        this.getInterceptors().add(lri);
        this.setRequestFactory(LoggingRequestFactoryFactory.build(lri));
        //errors handling
        setErrorHandler(new RestTemplateErrorHandler());
    }

    private void interceptorsIntegration(List<ClientHttpRequestInterceptor> lInterceptors) {
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
        this.setErrorHandler(new RestTemplateErrorHandler());
        if (!CollectionUtils.isEmpty(lConverters)) {
            //emptiness is rechecked inside setMessageConverters but it may change 
            //in a future spring release
            setMessageConverters(lConverters);
        }
        if (lInterceptors == null) {
            lInterceptors = new ArrayList<>();
        }
        interceptorsIntegration(lInterceptors);
    }

    private <T> T postForPathPrivate(MultiValueMap headers, Path temp, String url, Class<T> returnType) throws RestClientException {
        headers = checkHttpHeaders(headers);
        HttpEntity<Resource> he = new HttpEntity<>(new FileSystemResource(temp.toFile()), headers);
        return this.postForObject(url, he, returnType);
    }

    /**
     *
     * post a file to a service, the post is executed in a pseudo-streaming
     * mode, which means TCP segments are sent toward the url immediately as
     * soon as the sufficient data is read inside the input file. So you will
     * not have any problem with memory management especially if you have to
     * deal with big files.
     *
     * @param <T>: the return type of the webmethod
     * @param url: the url toward which a file (digital object) will be sent
     * @param file: the file to upload toward the service
     * @param returnType: the return type of the webmethod
     * @param headers: a map of headers you want to attach to your request
     * (<b>NB:</b> handling your headers by an interceptor would slow down your
     * query execution). By the way, even the LoggingRequestInterceptor should
     * not be used here
     * @return
     */
    public <T> T postForPath(String url, Path file, Class<T> returnType, MultiValueMap headers) {
        return postForPathPrivate(headers, file, url, returnType);
    }

    /**
     *
     * post a file to a service, the post is executed in a pseudo-streaming
     * mode, which means TCP segments are sent toward the url immediately as
     * soon as the sufficient data is read inside the input file. So you will
     * not have any problem with memory management especially if you have to
     * deal with big files.
     * <b>NB:</b> handling your headers by an interceptor would slow down your
     * query execution, hencc if you have to use headers, another method will
     * accept them as arguments. By the way, even the LoggingRequestInterceptor
     * should not be used here
     *
     * @param <T>: the return type of the webmethod
     * @param url: the url toward which a file (digital object) will be sent
     * @param file: the file to upload toward the service
     * @param returnType: the return type of the webmethod
     * @return
     */
    public <T> T postForPath(String url, Path file, Class<T> returnType) {
        return postForPathPrivate(null, file, url, returnType);
    }

    /**
     *
     * post a file to a service, the post is executed in a pseudo-streaming
     * mode, which means TCP segments are sent toward the url immediately as
     * soon as the sufficient data is read inside the input file. So you will
     * not have any problem with memory management especially if you have to
     * deal with big files.
     *
     * When the upload is finished, your file will be deleted from your disk
     * space
     *
     * @param <T>: the return type of the webmethod
     * @param url: the url toward which a file (digital object) will be sent
     * @param file: the file to upload toward the service
     * @param returnType: the return type of the webmethod
     * @param headers: a map of headers you want to attach to your request
     * (<b>NB:</b> handling your headers by an interceptor would slow down your
     * query execution). By the way, even the LoggingRequestInterceptor should
     * not be used here
     * @return
     * @throws java.io.IOException
     */
    public <T> T postForPathAndDelete(String url, Path file, Class<T> returnType, MultiValueMap headers) throws IOException {
        T response = postForPathPrivate(headers, file, url, returnType);
        Files.delete(file);
        return response;
    }

    /**
     *
     * post a file to a service, the post is executed in a pseudo-streaming
     * mode, which means TCP segments are sent toward the url immediately as
     * soon as the sufficient data is read inside the input file. So you will
     * not have any problem with memory management especially if you have to
     * deal with big files.
     * <b>NB:</b> handling your headers by an interceptor would slow down your
     * query execution, hence if you have to use headers, another method will
     * accept them as arguments. By the way, even the LoggingRequestInterceptor
     * should not be used here
     *
     * When the upload is finished, your file will be deleted from your disk
     * space
     *
     * @param <T>: the return type of the webmethod
     * @param url: the url toward which a file (digital object) will be sent
     * @param file: the file to upload toward the service
     * @param returnType: the return type of the webmethod
     * @return
     * @throws java.io.IOException
     */
    public <T> T postForPathAndDelete(String url, Path file, Class<T> returnType) throws IOException {
        T response = postForPathPrivate(null, file, url, returnType);
        Files.delete(file);
        return response;
    }

    /**
     *
     * post a file to a service, the post is executed in a pseudo-streaming
     * mode, which means TCP segments are sent toward the url immediately as
     * soon as the sufficient data is read inside the input file. So you will
     * not have any problem with memory management especially if you have to
     * deal with big files.
     * <b>NB:</b> handling your headers by an interceptor would slow down your
     * query execution, hence if you have to use headers, another method will
     * accept them as arguments. By the way, even the LoggingRequestInterceptor
     * should not be used here
     * <b>NB:</b> also you should know Path from java.nio is known to be more
     * performant than File from java.io and can provide all File class
     * capabilities
     *
     * When the upload is finished, your file will be deleted from your disk
     * space
     *
     * @param <T>: the return type of the webmethod
     * @param url: the url toward which a file (digital object) will be sent
     * @param file: the file to upload toward the service
     * @param returnType: the return type of the webmethod
     * @return
     * @throws java.io.IOException
     */
    public <T> T postForFileAndDelete(String url, File file, Class<T> returnType) throws IOException {
        Path p = file.toPath();
        T response = postForPathPrivate(null, p, url, returnType);
        Files.delete(p);
        return response;
    }

    private Path getForObjectPrivate(String serviceUrl, String tmpFilePath, MultiValueMap headers) {
        headers = checkHttpHeaders(headers);
        final Map singleValueMap = headers.toSingleValueMap();
        final Path temp = Paths.get(tmpFilePath);
        RequestCallback requestCallback = (ClientHttpRequest request) -> {
            request.getHeaders().setAll(singleValueMap);
        };
        ResponseExtractor<Void> responseExtractor = (ClientHttpResponse response) -> {
            Files.copy(response.getBody(), temp, StandardCopyOption.REPLACE_EXISTING);
            return null;
        };
        this.execute(serviceUrl, HttpMethod.GET, requestCallback, responseExtractor);
        return temp;
    }

    private MultiValueMap checkHttpHeaders(MultiValueMap headers) {
        if (headers == null) {
            headers = new LinkedMultiValueMap();
        }
        if (!headers.containsKey(HttpHeaders.CONTENT_TYPE)) {
            headers.add(HttpHeaders.CONTENT_TYPE, MimeTypeUtils.APPLICATION_OCTET_STREAM_VALUE);
        }
        return headers;
    }

    /**
     * download a file from a url and retrieve a file stored on disk space, can
     * be a temporary file. The file is downloaded in streaming, which means the
     * file is being written at the same time the service replies its TCP
     * segments, so you will not have any problem with memory management
     * especially if you have to deal with big files.
     * <b>NB:</b> handling your headers by an interceptor would slow down your
     * query execution. By the way, even the LoggingRequestInterceptor should
     * not be used here
     *
     * @param serviceUrl
     * @param tmpFilePath
     * @param headers
     * @return
     */
    public Path getForObject(String serviceUrl, String tmpFilePath, MultiValueMap headers) {
        return getForObjectPrivate(serviceUrl, tmpFilePath, headers);
    }

    /**
     * download a file from a url and retrieve a file stored on disk space, can
     * be a temporary file. The file is downloaded in streaming, which means the
     * file is being written at the same time the service replies its TCP
     * segments, so you will not have any problem with memory management
     * especially if you have to deal with big files.
     * <b>NB:</b> handling your headers by an interceptor would slow down your
     * query execution, hence if you have to use headers, another method will
     * accept them as arguments. By the way, even the LoggingRequestInterceptor
     * should not be used here
     *
     * @param serviceUrl
     * @param tmpFilePath
     * @return
     */
    public Path getForObject(String serviceUrl, String tmpFilePath) {
        return getForObjectPrivate(serviceUrl, tmpFilePath, null);
    }

    /**
     * download a file from a url and retrieve a file stored on disk space, can
     * be a temporary file. The file is downloaded in streaming, which means the
     * file is being written at the same time the service replies its TCP
     * segments, so you will not have any problem with memory management
     * especially if you have to deal with big files.
     * <b>NB:</b> handling your headers by an interceptor would slow down your
     * query execution, hence if you have to use headers, another method will
     * accept them as arguments. By the way, even the LoggingRequestInterceptor
     * should not be used here
     * <b>NB:</b> also you should know Path from java.nio is known to be more
     * performant than File from java.io and can provide all File class
     * capabilities
     *
     * @param serviceUrl
     * @param tmpFilePath
     * @return
     */
    public File getForObjectAsFile(String serviceUrl, String tmpFilePath) {
        return getForObjectPrivate(serviceUrl, tmpFilePath, null).toFile();
    }

}
