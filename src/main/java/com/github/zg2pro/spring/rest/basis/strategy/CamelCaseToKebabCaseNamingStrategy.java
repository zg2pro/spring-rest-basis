package com.github.zg2pro.spring.rest.basis.strategy;

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
