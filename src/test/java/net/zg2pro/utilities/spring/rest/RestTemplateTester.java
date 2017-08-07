package net.zg2pro.utilities.spring.rest;

import java.util.ArrayList;
import java.util.List;
import net.zg2pro.utilities.spring.Application;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.junit4.SpringRunner;

/**
 *
 * @author zg2pro
 */
@RunWith(SpringRunner.class)
//@RunWith(SpringJUnit4ClassRunner.class)
//@RestClientTest(MockedControllers.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = Application.class)
public class RestTemplateTester {

    @Autowired
    private TestRestTemplate rt;

//    @LocalServerPort
//    int randomServerPort;
    @Test
    public void testLogger() {
        //RestTemplate rt = new RestTemplate();
        // System.err.println(randomServerPort);
        List<ClientHttpRequestInterceptor> lInterceptors = new ArrayList<>();
        lInterceptors.add(new LoggingRequestInterceptor());
        rt.getRestTemplate().setInterceptors(lInterceptors);
        ResponseEntity<String> resp = rt.getForEntity("/testLogger", String.class);
        assertThat(resp.getBody()).isEqualTo("Hello World");
    }

}
