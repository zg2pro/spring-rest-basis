package com.github.zg2pro.spring.rest.basis.strategy;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import org.junit.Test;

/**
 *
 * unit tests about the camelToKebabCase
 *
 * @author zg2pro
 */
public class CamelToKebabTest {

    @Test
    public void testSomeValues() {
        CamelCaseToKebabCaseNamingStrategy cctkcns = new CamelCaseToKebabCaseNamingStrategy();
        assertThat(cctkcns.translate(null)).isNullOrEmpty();
        assertThat(cctkcns.translate("k")).isEqualTo("k");
        //you sould not put 2 capitals together but if you do thats what you gonna have
        assertThat(cctkcns.translate("aTestForARandomKebab")).isEqualTo("a-test-for-arandom-kebab");
        assertThat(cctkcns.translate("TestForARandomKebab")).isEqualTo("test-for-arandom-kebab");
    }
}
