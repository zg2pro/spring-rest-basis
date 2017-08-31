package com.github.zg2pro.spring.rest.basis.streaming;

import com.github.zg2pro.spring.rest.basis.*;
import static com.github.zg2pro.spring.rest.basis.MockedControllers.TEST_URL_FILE_DOWNLOAD;
import static com.github.zg2pro.spring.rest.basis.MockedControllers.TEST_URL_FILE_UPLOAD;
import com.github.zg2pro.spring.rest.basis.template.Zg2proRestTemplate;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.LocalHostUriTemplateHandler;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.LinkedMultiValueMap;

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
    @Primary
    public TestRestTemplate zg2TestRestTemplate(ObjectProvider<RestTemplateBuilder> builderProvider, Environment environment) {
        Zg2proRestTemplate rt = new Zg2proRestTemplate();
        TestRestTemplate trt = new TestRestTemplate(rt);
        trt.setUriTemplateHandler(new LocalHostUriTemplateHandler(environment));
        return trt;
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
public class StreamingTest {

    @Autowired
    private TestRestTemplate rt;

    private Path originalFile;
    
    @Before
    public void init(){
        ClassLoader classLoader = getClass().getClassLoader();
        originalFile = new File(classLoader.getResource("com/github/zg2pro/spring/rest/basis/streaming/test-binary.JPG").getFile()).toPath();
    }
    
    @Test
    public void testStream() throws IOException {
        String s = ((Zg2proRestTemplate) rt.getRestTemplate()).postForPath(TEST_URL_FILE_UPLOAD, originalFile, String.class);
        assertThat(s).isEqualTo("ok");
        
        LinkedMultiValueMap httpHeaders = new LinkedMultiValueMap();
        httpHeaders.add("XX-AUTH-TOKEN", UUID.randomUUID().toString());
        ((Zg2proRestTemplate) rt.getRestTemplate()).setFilesStreamingOperationsHttpHeaders(httpHeaders);        
        
        Path sp = ((Zg2proRestTemplate) rt.getRestTemplate()).getForObject(TEST_URL_FILE_DOWNLOAD, "target/test-content.tmp");
        byte[] newFile = Files.readAllBytes(sp);
        Files.delete(sp);
        byte[] oldFile = Files.readAllBytes(originalFile);
        assertThat(oldFile).isEqualTo(newFile);
    }

}
