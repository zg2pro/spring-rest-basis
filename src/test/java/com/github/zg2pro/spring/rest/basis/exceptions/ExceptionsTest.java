package com.github.zg2pro.spring.rest.basis.exceptions;

import com.github.zg2pro.spring.rest.basis.*;
import static com.github.zg2pro.spring.rest.basis.MockedControllers.EXCEPTION_MESSAGE;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.fail;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.client.RestClientException;

@ControllerAdvice
class ServiceAdvisor extends Zg2proRestServerExceptionsHandler {

}

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
public class ExceptionsTest {

    @Autowired
    private TestRestTemplate rt;

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

}
