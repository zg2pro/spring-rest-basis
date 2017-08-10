package net.zg2pro.utilities.spring.rest.template;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import net.zg2pro.utilities.spring.rest.interceptors.LoggingRequestInterceptor;
import net.zg2pro.utilities.spring.rest.strategy.CamelCaseToKebabCaseNamingStrategy;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.InterceptingClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

/**
 *
 * Layer over spring's RestTemplate loading automatically the lib utilities
 * 
 * @author zg2pro
 */
public class Zg2proRestTemplate extends RestTemplate {


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
    
    public static ObjectMapper camelToKebabObjectMapper() {
        ObjectMapper jsonMapper = new ObjectMapper();
        jsonMapper.setPropertyNamingStrategy(new CamelCaseToKebabCaseNamingStrategy());
        return jsonMapper;
    }
    
    /**
     * a RestTemplate including logging interceptor
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
        List<ClientHttpRequestInterceptor> lInterceptors = new ArrayList<>();
        lInterceptors.add(new LoggingRequestInterceptor());
        interceptorsIntegration(lInterceptors);
    }

    /**
     * a RestTemplate including your arguments for message converters and interceptors
     * 
     * @param lConverters - among which could jackson customized with the CamelCaseToKebabCase policy
     * @param lInterceptors - among which could be LoggingRequestInterceptor
     */
    public Zg2proRestTemplate(List<HttpMessageConverter<?>> lConverters,
            List<ClientHttpRequestInterceptor> lInterceptors) {
        super();
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
