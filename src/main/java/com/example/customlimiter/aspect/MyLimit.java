package com.example.customlimiter.aspect;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author ZhangChen
 * Created in 2024/1/7 20:35
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface MyLimit {
    /**
     * @return 限流key
     * Key需要是Spring Express 表达式： #user?.id
     */
    String limitKey() default "";
}
