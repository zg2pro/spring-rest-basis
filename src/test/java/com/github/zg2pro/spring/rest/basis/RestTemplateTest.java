package com.github.zg2pro.spring.rest.basis;

import static com.github.zg2pro.spring.rest.basis.MockedControllers.EXCEPTION_MESSAGE;
import java.nio.charset.StandardCharsets;
import com.github.zg2pro.spring.rest.basis.interceptors.LoggingRequestInterceptor;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import static com.github.zg2pro.spring.rest.basis.MockedControllers.TEST_URL_GET;
import static com.github.zg2pro.spring.rest.basis.MockedControllers.TEST_URL_GET_BLANK_REPLY;
import static com.github.zg2pro.spring.rest.basis.MockedControllers.TEST_URL_GET_LONG_REPLY;
import static com.github.zg2pro.spring.rest.basis.MockedControllers.TEST_URL_GET_STRUCTURE;
import com.github.zg2pro.spring.rest.basis.exceptions.RestTemplateErrorHandler;
import com.github.zg2pro.spring.rest.basis.exceptions.RestTemplateException;
import com.github.zg2pro.spring.rest.basis.exceptions.StackTracedException;
import com.github.zg2pro.spring.rest.basis.exceptions.Zg2proRestServerExceptionsHandler;
import com.github.zg2pro.spring.rest.basis.interceptors.LoggingRequestFactoryFactory;
import com.github.zg2pro.spring.rest.basis.template.Zg2proRestTemplate;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.fail;
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
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClientException;

@RestController
class MockedControllers {

    protected static final String TEST_RETURN_VALUE = "hello zg2pro!";
    protected static final String TEST_URL_GET = "/testLogger";
    protected static final String TEST_URL_GET_BLANK_REPLY = "/testLoggerBlankReply";
    protected static final String TEST_URL_GET_LONG_REPLY = "/testLoggerLongReply";
    protected static final String TEST_URL_GET_STRUCTURE = "/testStructure";
    protected static final String TEST_URL_ERROR_REPLY = "/errorReply";

    protected static final String EXCEPTION_MESSAGE = "testing an execption serialization";

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
    String testLoggerLongReply() {
        byte[] randomBytes = new byte[100000];
        new Random().nextBytes(randomBytes);
        return new String(randomBytes);
    }

    @RequestMapping(value = TEST_URL_GET_STRUCTURE, method = RequestMethod.GET)
    public @ResponseBody
    ReturnedStructure testStructureReply() {
        ReturnedStructure rs = new ReturnedStructure();
        rs.setFieldOne(12);
        rs.setFieldTwo("test string value");
        rs.setFieldThree(0.8965);
        return rs;
    }

    @RequestMapping(value = TEST_URL_ERROR_REPLY, method = RequestMethod.GET)
    public @ResponseBody
    ReturnedStructure testError() {
        throw new NullPointerException(EXCEPTION_MESSAGE);
    }
}

@ControllerAdvice
class ServiceAdvisor extends Zg2proRestServerExceptionsHandler {

}

class ReturnedStructure {

    private int fieldOne;
    private String fieldTwo;
    private double fieldThree;

    public int getFieldOne() {
        return fieldOne;
    }

    public void setFieldOne(int fieldOne) {
        this.fieldOne = fieldOne;
    }

    public String getFieldTwo() {
        return fieldTwo;
    }

    public void setFieldTwo(String fieldTwo) {
        this.fieldTwo = fieldTwo;
    }

    public double getFieldThree() {
        return fieldThree;
    }

    public void setFieldThree(double fieldThree) {
        this.fieldThree = fieldThree;
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

    @Bean
    public ServiceAdvisor serviceAdvisor() {
        return new ServiceAdvisor();
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
public class RestTemplateTest {

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
        List<HttpMessageConverter<?>> z = new Zg2proRestTemplate().getMessageConverters();
        ResponseEntity<String> resp;
        for (Level l : Level.values()) {
            rt.getRestTemplate().setRequestFactory(LoggingRequestFactoryFactory.build(StandardCharsets.UTF_8, 1000, l));
            for (String str : new String[]{TEST_URL_GET_LONG_REPLY, TEST_URL_GET}) {
                resp = rt.getForEntity(str, String.class);
                assertThat(resp.getBody()).isNotNull();
            }
        }
    }

    @Test
    public void testError() {
        rt.getRestTemplate().setErrorHandler(new RestTemplateErrorHandler());
        try {
            rt.getForObject(MockedControllers.TEST_URL_ERROR_REPLY, ReturnedStructure.class);
            fail("we should not get to this point");
        } catch (RestTemplateException rte) {
            StackTracedException ste = (StackTracedException) rte.getCause();
            assertThat(ste.getCause()).isInstanceOf(NullPointerException.class);
            assertThat(ste.getMessage()).contains(EXCEPTION_MESSAGE);
        } catch (RestClientException e) {
            fail("got a restClientException: " + e.getClass());
        } catch (Throwable e) {
            fail("the error was not rightly retrieved: " + e.getClass());
        }
    }

    @Test
    public void testZg2Template() {
        Zg2proRestTemplate z = new Zg2proRestTemplate();
        assertThat(z.getErrorHandler()).isInstanceOf(RestTemplateErrorHandler.class);
        rt.getRestTemplate().setRequestFactory(z.getRequestFactory());
        ResponseEntity<String> resp;
        resp = rt.getForEntity(MockedControllers.TEST_URL_GET_BLANK_REPLY, String.class);
        assertNotNull(resp);
        ReturnedStructure rs = rt.getForObject(TEST_URL_GET_STRUCTURE, ReturnedStructure.class);
        assertThat(rs.getFieldOne()).isEqualTo(12);
    }

}
