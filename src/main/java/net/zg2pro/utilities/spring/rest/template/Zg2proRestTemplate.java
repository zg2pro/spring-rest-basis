package net.zg2pro.utilities.spring.rest.template;

import java.util.ArrayList;
import java.util.List;
import net.zg2pro.utilities.spring.rest.interceptors.LoggingRequestInterceptor;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.InterceptingClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
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
    
    /**
     * a RestTemplate including logging interceptor
     */
    public Zg2proRestTemplate() {
        super();
        List<ClientHttpRequestInterceptor> lInterceptors = new ArrayList<>();
        lInterceptors.add(new LoggingRequestInterceptor());
        interceptorsIntegration(lInterceptors);
    }

    /**
     * a RestTemplate including your argument interceptors among which
     * could be the LoggingRequestInterceptor
     * @param list
     * @param lInterceptors
     */
    public Zg2proRestTemplate(List<HttpMessageConverter<?>> list,
            List<ClientHttpRequestInterceptor> lInterceptors) {
        super();
        if (!CollectionUtils.isEmpty(list)) {
            //emptiness is rechecked inside setMessageConverters but it may change 
            //in a future spring release
            setMessageConverters(list);
        }
        if (!CollectionUtils.isEmpty(lInterceptors)) {
            interceptorsIntegration(lInterceptors);
        }
    }
}
