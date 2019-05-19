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
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
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
 * @since 0.3
 * 
 * @author zg2pro
 */
public abstract class AbstractZg2proRestTemplate extends RestTemplate {

    private List<ClientHttpRequestInterceptor> lInterceptors;
    private MultiValueMap filesStreamingOperationsHttpHeaders;

    @Override
    public List<ClientHttpRequestInterceptor> getInterceptors() {
        return lInterceptors;
    }

    @Override
    public void setInterceptors(List<ClientHttpRequestInterceptor> interceptors) {
        this.lInterceptors = interceptors;
    }

    public MultiValueMap getFilesStreamingOperationsHttpHeaders() {
        return filesStreamingOperationsHttpHeaders;
    }

    /**
     *
     * @param filesStreamingOperationsHttpHeaders: a map of headers you want to
     * attach to your request (<b>NB:</b> handling your headers by an
     * interceptor would slow down your query execution). By the way, even the
     * LoggingRequestInterceptor should not be used here
     */
    public void setFilesStreamingOperationsHttpHeaders(MultiValueMap filesStreamingOperationsHttpHeaders) {
        if (filesStreamingOperationsHttpHeaders == null) {
            filesStreamingOperationsHttpHeaders = new LinkedMultiValueMap();
        }
        if (!filesStreamingOperationsHttpHeaders.containsKey(HttpHeaders.CONTENT_TYPE)) {
            filesStreamingOperationsHttpHeaders.add(HttpHeaders.CONTENT_TYPE, MimeTypeUtils.APPLICATION_OCTET_STREAM_VALUE);
        }
        this.filesStreamingOperationsHttpHeaders = filesStreamingOperationsHttpHeaders;
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

    protected AbstractZg2proRestTemplate(SimpleModule sm) {
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
        //errors handling
        this.setErrorHandler(new RestTemplateErrorHandler());
        /*
        //interceptors
        LoggingRequestInterceptor lri = new LoggingRequestInterceptor();
        this.setInterceptors(new ArrayList<>());
        this.getInterceptors().add(lri);
        this.setRequestFactory(LoggingRequestFactoryFactory.build(lri));
        */
    }

    protected abstract void interceptorsIntegration(List<ClientHttpRequestInterceptor> lInterceptors, Object sslConfiguration);

    protected AbstractZg2proRestTemplate(@Nullable List<HttpMessageConverter<?>> lConverters,
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
    }

    private <T> T postForPathPrivate(Path temp, String url, Class<T> returnType) throws RestClientException {
        HttpEntity<Resource> he = new HttpEntity<>(new FileSystemResource(temp.toFile()), getFilesStreamingOperationsHttpHeaders());
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
     * @return the response
     */
    public <T> T postForPath(String url, Path file, Class<T> returnType) {
        return postForPathPrivate(file, url, returnType);
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
     * @return the response
     * @throws java.io.IOException
     */
    public <T> T postForPathAndDelete(String url, Path file, Class<T> returnType) throws IOException {
        T response = postForPathPrivate(file, url, returnType);
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
        T response = postForPathPrivate(p, url, returnType);
        Files.delete(p);
        return response;
    }

    private Path getForObjectPrivate(String serviceUrl, String tmpFilePath) {
        final Map singleValueMap = getFilesStreamingOperationsHttpHeaders().toSingleValueMap();
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
     * @param serviceUrl service url of the file
     * @param tmpFilePath file path for the file
     * @return the path object
     */
    public Path getForObject(String serviceUrl, String tmpFilePath) {
        return getForObjectPrivate(serviceUrl, tmpFilePath);
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
     * @param serviceUrl service url of the file
     * @param tmpFilePath file path for the file
     * @return the file object
     */
    public File getForObjectAsFile(String serviceUrl, String tmpFilePath) {
        return getForObjectPrivate(serviceUrl, tmpFilePath).toFile();
    }

}
