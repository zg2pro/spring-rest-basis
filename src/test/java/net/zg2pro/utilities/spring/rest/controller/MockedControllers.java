package net.zg2pro.utilities.spring.rest.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author zg2pro
 */
@RestController
public class MockedControllers {

    @RequestMapping(value = "/testLogger", method = RequestMethod.GET)
    public  @ResponseBody String testLogger() {
        return "Hello World";
    }
}
