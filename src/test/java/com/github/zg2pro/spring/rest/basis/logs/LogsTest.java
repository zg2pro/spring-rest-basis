package com.github.zg2pro.spring.rest.basis.logs;

import com.github.zg2pro.spring.rest.basis.MockedControllers;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import static com.github.zg2pro.spring.rest.basis.MockedControllers.TEST_URL_GET;
import static com.github.zg2pro.spring.rest.basis.MockedControllers.TEST_URL_GET_LONG_REPLY;
import static com.github.zg2pro.spring.rest.basis.MockedControllers.TEST_URL_GET_STRUCTURE;
import com.github.zg2pro.spring.rest.basis.ReturnedStructure;
import com.github.zg2pro.spring.rest.basis.exceptions.RestTemplateErrorHandler;
import com.github.zg2pro.spring.rest.basis.template.Zg2proRestTemplate;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.Assert.assertNotNull;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.event.Level;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.InterceptingClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Spring boot server (tomcat embedded) runner
 *
 * @author zg2pro
 */
@RunWith(MockitoJUnitRunner.class)
@EnableAutoConfiguration
@Configuration
class ApplicationBoot {

    @Bean
    public MockedControllers mockedControllers() {
        return new MockedControllers();
    }

}

/**
 *
 * unit tests about the rest template and its interceptor
 *
 * @author zg2pro
 */
@RunWith(SpringRunner.class)
@SpringBootTest(
        classes = {ApplicationBoot.class},
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class LogsTest {

    @Autowired
    private TestRestTemplate rt;

    @Test(expected = IllegalArgumentException.class)
    public void constructions1() {
        assertNotNull(new LoggingRequestInterceptor(StandardCharsets.UTF_8, -4, Level.INFO));
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructions2() {
        assertNotNull(new LoggingRequestInterceptor(null, 1000, Level.INFO));
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructions3() {
        assertNotNull(new LoggingRequestInterceptor(StandardCharsets.ISO_8859_1, 10000, null));
    }

    /**
     * hard to check the logs provided by the interceptor when there's no error
     * however this unit test garantees the interceptor does not alter the reply
     * from the rest service.
     */
    @Test
    public void testInterceptor() {
        List<ClientHttpRequestInterceptor> lInterceptors = new ArrayList<>();
        //spring boot default log level is info
        lInterceptors.add(new LoggingRequestInterceptor(StandardCharsets.ISO_8859_1, 100, Level.ERROR));
        SimpleClientHttpRequestFactory chrf = new SimpleClientHttpRequestFactory();
        chrf.setOutputStreaming(false);
        rt.getRestTemplate().setRequestFactory(new InterceptingClientHttpRequestFactory(
                new BufferingClientHttpRequestFactory(chrf),
                lInterceptors
        ));
        ResponseEntity<String> resp = rt.getForEntity(MockedControllers.TEST_URL_GET, String.class);
        assertThat(resp.getBody()).isEqualTo(MockedControllers.TEST_RETURN_VALUE);
    }

    @Test
    public void testsWithLogLevels() {
        ResponseEntity<String> resp;
        for (Level l : Level.values()) {
            rt.getRestTemplate().setRequestFactory(
                    LoggingRequestFactoryFactory.build(new LoggingRequestInterceptor(StandardCharsets.UTF_8, 1000, l))
            );
            for (String str : new String[]{TEST_URL_GET_LONG_REPLY, TEST_URL_GET}) {
                resp = rt.getForEntity(str, String.class);
                assertThat(resp.getBody()).isNotNull();
            }
        }
    }

    @Test
    public void testZg2Template() {
        Zg2proRestTemplate z0 = new Zg2proRestTemplate();
        assertThat(z0.getErrorHandler()).isInstanceOf(RestTemplateErrorHandler.class);
        assertThat(z0.getInterceptors().size()).isGreaterThan(0);
        List<ClientHttpRequestInterceptor> lInterceptors = new ArrayList<>();
        lInterceptors.add(new LoggingRequestInterceptor());
        List<HttpMessageConverter<?>> covs = z0.getMessageConverters();
        z0 = new Zg2proRestTemplate(null, lInterceptors);
        assertThat(z0).isNotNull();
        Zg2proRestTemplate z = new Zg2proRestTemplate(covs, null);
        z.setInterceptors(lInterceptors);
        assertThat(z.getInterceptors().size()).isGreaterThan(0);
        z.setRequestFactory(LoggingRequestFactoryFactory.build());
        assertThat(z.getInterceptors().size()).isGreaterThan(0);
        assertThat(z.getErrorHandler()).isInstanceOf(RestTemplateErrorHandler.class);
        rt.getRestTemplate().setRequestFactory(z.getRequestFactory());
        ResponseEntity<String> resp;
        resp = rt.getForEntity(MockedControllers.TEST_URL_GET_BLANK_REPLY, String.class);
        assertNotNull(resp);
        ReturnedStructure rs = rt.getForObject(TEST_URL_GET_STRUCTURE, ReturnedStructure.class);
        assertThat(rs.getFieldOne()).isEqualTo(12);
    }

}
