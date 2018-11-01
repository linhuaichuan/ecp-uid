package com.myzmds.ecp.core.uid.util;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Lazy(false)
@Component
public class SpringUtils implements ApplicationContextAware {
    private static ApplicationContext context;
    
    public static ApplicationContext getContext() {
        return context;
    }
    
    public static Object getBean(String beanId) {
        return getContext().getBean(beanId);
    }
    
    public static <T> T getBean(Class<T> beanClass) {
        return getContext().getBean(beanClass);
    }
    
    @Override
    public void setApplicationContext(ApplicationContext context)
        throws BeansException {
        SpringUtils.context = context;
    }
}