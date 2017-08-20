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

import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * error resource exception generator
 *
 * @author zg2pro
 */
public class StackTracedException extends Exception {

    private static final long serialVersionUID = 3450724120497219309L;

    private static final Logger logger = LoggerFactory.getLogger(StackTracedException.class);

    /**
     * generates a stack trace from a serialized equivalent
     *
     * @param lste
     * @return
     */
    public static StackTraceElement[] mapper(Zg2proStackTrace lste) {
        List<StackTraceElement> stack = new ArrayList<>();
        for (Zg2proStackTraceElement nste : lste) {
            StackTraceElement ste = new StackTraceElement(nste.getDeclaringClass(),
                    nste.getMethodName(), nste.getFileName(), nste.getLineNumber());
            stack.add(ste);
        }
        return stack.toArray(new StackTraceElement[stack.size()]);
    }

    /**
     * constructor
     *
     * @param nestErrorResource
     */
    public StackTracedException(ErrorResource nestErrorResource) {
        super("HttpStatus:" + nestErrorResource.getCode() + " - " + nestErrorResource.getErrorMessage());
        try {
            // Non predifined error class. So we must use reflection method 
            Class excClazz = Class.forName(nestErrorResource.getErrorClassName());
            Object o = excClazz.newInstance();
            ((Throwable) o).setStackTrace(mapper(nestErrorResource.getStackTrace()));
            initCause((Throwable) o);
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | ClassCastException cnfe) {
            super.setStackTrace(mapper(nestErrorResource.getStackTrace()));
            logger.warn("exception occured when mapping an exception {}", cnfe);
        }
    }
}
