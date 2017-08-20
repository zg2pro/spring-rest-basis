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

/**
 * Manages errors triggered when querying the business application, when
 * serializing, or when deserializing
 *
 * @author zg2pro
 */
public class RestTemplateException extends RuntimeException {

    private static final long serialVersionUID = -8401400592804985312L;

    /**
     * default constructor
     */
    public RestTemplateException() {
        super();
    }

    /**
     * constructor from error message and "caused by"-element error resource
     *
     * @param message
     * @param er
     */
    public RestTemplateException(String message, ErrorResource er) {
        super(message, new StackTracedException(er));
    }

    /**
     * constructor from "caused by"-element error resource
     *
     * @param er
     */
    public RestTemplateException(ErrorResource er) {
        super(er.getErrorClassName() + " error raised by rest client: " + er.getErrorMessage());
        super.setStackTrace(StackTracedException.mapper(er.getStackTrace()));
    }

    /**
     * constructor from error message and "caused by"-element throwable
     *
     * @param message
     * @param cause
     */
    public RestTemplateException(String message, Throwable cause) {
        super(message, cause);
    }

}
