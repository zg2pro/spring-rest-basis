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
package com.github.zg2pro.spring.rest.basis.serialization;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;

/**
 *
 * use this class as naming strategy in your spring-web RestTemplate configuration, and it will 
 * help you turning camleCase to kebab and kebab to camel automatically without using any JsonProperty annotation 
 * or whatever. Works fine too if you want to do REST-XML.
 * 
 * @author zg2pro
 */
public class CamelCaseToKebabCaseNamingStrategy extends PropertyNamingStrategy.PropertyNamingStrategyBase {

    private static final long serialVersionUID = -8287187427679464871L;

    private static final char DASH = '-';
    
    @Override
    public String translate(String input) {
        if (input == null) {
            // garbage in, garbage out
            return input;
        }
        int length = input.length();
        StringBuilder result = new StringBuilder(length * 2);
        int resultLength = 0;
        boolean wasPrevTranslated = false;
        for (int i = 0; i < length; i++) {
            char c = input.charAt(i);
            if (i > 0 || c != DASH) {
                // skip first starting underscore
                if (Character.isUpperCase(c)) {
                    resultLength = endOfWord(wasPrevTranslated, resultLength, result);
                    c = Character.toLowerCase(c);
                    wasPrevTranslated = true;
                } else {
                    wasPrevTranslated = false;
                }
                result.append(c);
                resultLength++;
            }
        }
        return resultLength > 0 ? result.toString() : input;
    }

    private int endOfWord(boolean wasPrevTranslated, int resultLength, StringBuilder result) {
        if (!wasPrevTranslated && resultLength > 0 && result.charAt(resultLength - 1) != DASH) {
            result.append(DASH);
            resultLength++;
        }
        return resultLength;
    }
}
