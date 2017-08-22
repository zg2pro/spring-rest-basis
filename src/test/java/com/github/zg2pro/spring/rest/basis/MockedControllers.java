package com.github.zg2pro.spring.rest.basis;

import java.util.Random;
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

    public static final String TEST_RETURN_VALUE = "hello zg2pro!";
    public static final String TEST_URL_GET = "/testLogger";
    public static final String TEST_URL_GET_BLANK_REPLY = "/testLoggerBlankReply";
    public static final String TEST_URL_GET_LONG_REPLY = "/testLoggerLongReply";
    public static final String TEST_URL_GET_STRUCTURE = "/testStructure";
    public static final String TEST_URL_ERROR_REPLY = "/errorReply";

    public static final String EXCEPTION_MESSAGE = "testing an execption serialization";

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
