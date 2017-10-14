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
package com.github.zg2pro.spring.rest.basis.security;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

/**
 *
 * This class will help you to check upon each build (if you use it jointly 
 * with a maven plugin) or upon each app start (if you want to declare a 
 * spring bean) whether all your remote methods have been secured with
 * a set of permissions
 * 
 * Conditions to make it work: your remote signatures must be placed in a
 * dedicated interface suffixed with "Remote" your local signatures must be
 * placed in an interface suffixed with "Local" however you can always extend
 * Remote or Local to have intermediary interfaces before declaring your
 * implementing class (for instance HelloWorldServiceImpl implements
 * HelloWorldServiceWebService, HelloWorldServiceLocal and
 * HelloWorldServiceWebService extends HelloWorldServiceRemote).
 *
 * @author zg2pro
 */
public class PreAuthorizeAllRemoteStrategy {

    private static final long serialVersionUID = -8287187427679464872L;

    private static final Logger logger = LoggerFactory.getLogger(PreAuthorizeAllRemoteStrategy.class);

    private static final String TEST_CLASS_FILENAME_ID = "Test";

    private final Class annotationUsedForPermissions;
    private final String rootPackageOfYourBeans;

    /**
     * You must declare in your code two classes: one for your permissions
     * enumeration ("MyPermissions") and a @interface annotation class,
     * containing the following field: MyPermissions[] value() default {};
     *
     * Please write your rootPackageOfYourBeans like xxx.yyy.zzz, it must be the
     * root package in which you include your implementations classes (annotated
     * with @Service or @Component)
     *
     * @param annotationUsedForPermissions
     * @param rootPackageOfYourBeans
     */
    public PreAuthorizeAllRemoteStrategy(Class annotationUsedForPermissions, String rootPackageOfYourBeans) {
        this.annotationUsedForPermissions = annotationUsedForPermissions;
        this.rootPackageOfYourBeans = rootPackageOfYourBeans;
    }

    private boolean isNotTestClass(Resource resource) {
        return !resource.getFilename().contains(TEST_CLASS_FILENAME_ID);
    }

    private Class parseClassNameWithPackage(Resource resource) throws IOException, ClassNotFoundException {
        String resourcePath = resource.getURL().getPath();
        String className;
        if (resourcePath.contains("!")) {
            className = resourcePath.split("!")[1];
        } else {
            className = resourcePath.split("WEB-INF/classes")[1];
        }
        return Class.forName(className.replaceAll("/", ".").replaceAll("\\.class", "").substring(1));
    }

    private List<Class> findByAnntotationAndRootPackagePath(Class annotationClass, String packageClass) throws IOException, ClassNotFoundException {
        List<Class> serviceClasses = new ArrayList<>();
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource[] resources = resolver.getResources("classpath*:/" + packageClass + "/**/*.class");
        for (Resource resource : resources) {
            if (isNotTestClass(resource)) {
                Class serviceClassCandidate = parseClassNameWithPackage(resource);
                if (serviceClassCandidate.isAnnotationPresent(annotationClass)) {
                    serviceClasses.add(serviceClassCandidate);
                }
            }
        }
        return serviceClasses;
    }

    private List<Class> findServiceClasses() throws IOException, ClassNotFoundException {
        String servicesPath = rootPackageOfYourBeans.replaceAll("\\.", "/");
        logger.debug("checking all security locks have been put on web services, using path: {}", servicesPath);
        return findByAnntotationAndRootPackagePath(Service.class, servicesPath);
    }

    private Method searchRemote(Class clazz, Method meth) {
        Method m = null;
        for (Class clazzI : clazz.getInterfaces()) {
            if (clazzI.getSimpleName().endsWith("Remote")) {
                try {
                    m = clazzI.getMethod(meth.getName(), meth.getParameterTypes());
                    break;
                } catch (NoSuchMethodException ex) {
                    logger.trace("method does not exist in the Remote interface: ", ex);
                    return null;
                }
            } else if (!clazzI.getSimpleName().endsWith("Local")) {
                m = searchRemote(clazzI, meth);
            }
        }
        return m;
    }

    public void processVerification() throws ClassNotFoundException, NoSuchMethodException, IOException {
        Map<Method, Method> remoteToServ = new HashMap<>();
        for (Class c : findServiceClasses()) {
            // Used to check all services' methods. 
            //So we must use 'getDeclaredMethods' reflection method to do the job.
            for (Method beanMethod : c.getDeclaredMethods()) {
                Method interfaceMeth = searchRemote(c, beanMethod);
                if (interfaceMeth != null) {
                    remoteToServ.put(interfaceMeth, beanMethod);
                }
            }
        }
        for (Method remoteMethod : remoteToServ.keySet()) {
            if (!remoteMethod.isAnnotationPresent(annotationUsedForPermissions)) {
                throw new SecurityException("all remote methods must be annotated with @" 
                        + annotationUsedForPermissions.getSimpleName() + ": " + remoteMethod);
            }
            if (!remoteToServ.get(remoteMethod).isAnnotationPresent(PreAuthorize.class)) {
                throw new SecurityException("all remote methods must be annotated with @PreAuthorize: " 
                        + remoteMethod);
            }
        }
    }

}
