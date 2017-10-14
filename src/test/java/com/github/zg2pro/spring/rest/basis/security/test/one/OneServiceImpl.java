package com.github.zg2pro.spring.rest.basis.security.test.one;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

/**
 *
 * @author zg2pro
 */
@Service
public class OneServiceImpl implements OneServiceRemote, OneServiceLocal {

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
