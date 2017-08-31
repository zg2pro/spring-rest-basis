/*
 * The MIT License
 *
 * Copyright 2017 zg2pro.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.github.zg2pro.spring.rest.basis.exceptions;

import java.nio.file.AccessDeniedException;
import java.security.AccessControlException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 *
 * decorate this class inherited with a @ControllerAdvice and let it be scanned
 * as a component, thus all your exceptions will be serialized and transmited
 * toward rest clients
 *
 * @author zg2pro
 * @since 0.2
 */
public class Zg2proRestServerExceptionsHandler extends ResponseEntityExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(Zg2proRestServerExceptionsHandler.class);

    private ResponseEntity<Object> globalHandler(Exception exception, 
            WebRequest request, 
            String logMessage, 
            HttpStatus status) {
        logger.error(logMessage, exception);
        ErrorResource error = new ErrorResource(
                status, 
                exception.getMessage(), 
                exception.getClass().getName(), 
                exception.getStackTrace()
        );
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return handleExceptionInternal(exception, error, headers, status, request);
    }

    /**
     * for permissions not granted
     *
     * @param exception
     * @param request
     * @return 401 - BAD_REQUEST
     */
    @ExceptionHandler({AccessControlException.class, AccessDeniedException.class})
    public ResponseEntity<Object> permissionError(Exception exception, WebRequest request) {
        return globalHandler(exception, request, "You don't have the permissions: ",
                HttpStatus.UNAUTHORIZED);
    }

    /**
     * for java exceptions not managed in the code
     *
     * @param exception
     * @param request
     * @return 400 - BAD_REQUEST
     */
    @ExceptionHandler(Throwable.class)
    public ResponseEntity<Object> technicalError(Exception exception, WebRequest request) {
        return globalHandler(exception, request, "Something unexpected happened: ",
                HttpStatus.BAD_REQUEST);
    }

}
