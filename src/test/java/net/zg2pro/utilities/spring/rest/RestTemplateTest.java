package net.zg2pro.utilities.spring.rest;

import java.nio.charset.StandardCharsets;
import net.zg2pro.utilities.spring.rest.interceptors.LoggingRequestInterceptor;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import net.zg2pro.utilities.spring.rest.template.Zg2proRestTemplate;
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
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
class MockedControllers {

    public static final String TEST_RETURN_VALUE = "hello zg2pro!";
    public static final String TEST_URL_GET = "/testLogger";
    public static final String TEST_URL_GET_BLANK_REPLY = "/testLoggerBlankReply";
    public static final String TEST_URL_GET_LONG_REPLY = "/testLoggerLongReply";

    @RequestMapping(value = TEST_URL_GET, method = RequestMethod.GET)
    public @ResponseBody
    String testLogger() {
        return TEST_RETURN_VALUE;
    }

    @RequestMapping(value = TEST_URL_GET_BLANK_REPLY, method = RequestMethod.GET)
    public @ResponseBody
    String testLoggerBlank() {
        return "";
    }

    @RequestMapping(value = TEST_URL_GET_LONG_REPLY, method = RequestMethod.GET)
    public @ResponseBody
    String testLoggerLonngReply() {
        byte[] randomBytes = new byte[Integer.MAX_VALUE];
        new Random().nextBytes(randomBytes);
        return new String(randomBytes);
    }
}

/**
 * Spring boot server (tomcat embedded) runner
 *
 * @author Gregory
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
@SpringBootTest(classes = {ApplicationBoot.class}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class RestTemplateTest {

    @Autowired
    private TestRestTemplate rt;

    @Test(expected = IllegalArgumentException.class)
    public void constructions1() {
        assertNotNull(new LoggingRequestInterceptor(StandardCharsets.UTF_8, -4, Level.INFO));
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructions2() {
        assertNotNull(new LoggingRequestInterceptor(null, -4, Level.INFO));
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructions3() {
        assertNotNull(new LoggingRequestInterceptor(StandardCharsets.ISO_8859_1, -4, null));
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
    public void testZg2Template() {
        Zg2proRestTemplate z = new Zg2proRestTemplate();
        rt.getRestTemplate().setRequestFactory(z.getRequestFactory());
        ResponseEntity<String> resp;
        resp = rt.getForEntity(MockedControllers.TEST_URL_GET_LONG_REPLY, String.class);
        assertNotNull(resp);
        resp = rt.getForEntity(MockedControllers.TEST_URL_GET_BLANK_REPLY, String.class);
        assertNotNull(resp);
        List<ClientHttpRequestInterceptor> lInterceptors = new ArrayList<>();
        //spring boot default log level is info
        lInterceptors.add(new LoggingRequestInterceptor(StandardCharsets.UTF_8, 1000, Level.INFO));
        Zg2proRestTemplate z2 = new Zg2proRestTemplate(z.getMessageConverters(), lInterceptors);
        rt.getRestTemplate().setRequestFactory(z2.getRequestFactory());
        resp = rt.getForEntity(MockedControllers.TEST_URL_GET, String.class);
        assertThat(resp.getBody()).isEqualTo(MockedControllers.TEST_RETURN_VALUE);
    }

}
