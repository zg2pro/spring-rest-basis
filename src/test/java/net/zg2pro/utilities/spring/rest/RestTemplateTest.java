package net.zg2pro.utilities.spring.rest;

import java.util.ArrayList;
import java.util.List;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
class MockedControllers {

    public static final String TEST_RETURN_VALUE = "hello zg2pro!";
    public static final String TEST_URL_GET = "/testLogger";

    @RequestMapping(value = TEST_URL_GET, method = RequestMethod.GET)
    public @ResponseBody
    String testLogger() {
        return TEST_RETURN_VALUE;
    }
}

/**
 * Spring boot server (tomcat embedded) runner
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

    /**
     * hard to check the logs provided by the interceptor when there's no error
     * however this unit test garantees the interceptor does not alter the reply from
     * the rest service.
     */
    @Test
    public void testInterceptor() {
        List<ClientHttpRequestInterceptor> lInterceptors = new ArrayList<>();
        lInterceptors.add(new LoggingRequestInterceptor());
        rt.getRestTemplate().setInterceptors(lInterceptors);
        ResponseEntity<String> resp = rt.getForEntity(MockedControllers.TEST_URL_GET, String.class);
        assertThat(resp.getBody()).isEqualTo(MockedControllers.TEST_RETURN_VALUE);
    }
}
