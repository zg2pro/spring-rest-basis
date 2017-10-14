package com.github.zg2pro.spring.rest.basis.security.test.three;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

/**
 *
 * @author zg2pro
 */
@Service
public class ThreeServiceImpl implements ThreeServiceWebService, ThreeServiceLocal {

    @Override
    @PreAuthorize("checkPermissionAnnotation()")
    public boolean exampleMethod(String dummyArg) {
        return true;
    }

    @Override
    public boolean exampleLocalMethod(int dummyArg) {
        return true;
    }

}
