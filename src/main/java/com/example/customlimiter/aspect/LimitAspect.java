package com.example.customlimiter.aspect;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.example.customlimiter.cache.LimitChecker;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

/**
 * @author ZhangChen
 * Created in 2024/1/7 20:36
 */
@Aspect
@Component
@Slf4j
public class LimitAspect {

    @Resource
    private LimitChecker redisLimitChecker;

//
//    /**
//     * 对加有@RestController类下的所有方法生效
//     */
//    @Around("@within(org.springframework.web.bind.annotation.RestController)")
//    public Object aroundRestControllerAnno(ProceedingJoinPoint point) throws Throwable {
//        return process(point);
//    }

    /**
     * 对加有@MyLimit的方法生效
     */
    @Around("@annotation(com.example.customlimiter.aspect.Limiter)")
    public Object aroundLimitAnno(ProceedingJoinPoint point) throws Throwable {
        return process(point);
    }

    private Object process(ProceedingJoinPoint point) throws Throwable {
        log.info("======================into MyLimitAOP=======================");

        // 获取URI
        ServletRequestAttributes requestAttr =
                (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        String requestUri = requestAttr.getRequest().getRequestURI();

        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();
        Limiter limiterAnnotation = method.getAnnotation(Limiter.class);

        Object[] args = point.getArgs();
        //获取到请求参数
        Map<String, Object> fieldsName = getFieldsName(point);
        log.info("@MyLimit fieldsNameMap:{}", fieldsName);

        String limitKey = generateKey(limiterAnnotation, fieldsName);
        String limitCategory = requestUri;
        boolean canDo = redisLimitChecker.canDo(limitKey, limitCategory);

        // 放行
        if (canDo){
            log.info("limit pass. limitKey:{}, limitCategory:{}", limitKey, limitCategory);
            return point.proceed();
        }

        log.error("limit block. limitKey:{}, limitCategory:{}", limitKey, limitCategory);

        // 拦截
        throw new RuntimeException("限流");
    }


    private String generateKey(Limiter limiterAnnotation, Map<String, Object> fieldsName) {
        //获取注解上的值如 :  @MyLimit(limitKey = "#user?.id")
        String keyEl = limiterAnnotation.limitKey();
        log.info("@MyLimit limitKey:{}", keyEl);

        //创建解析器
        SpelExpressionParser parser = new SpelExpressionParser();
        //获取表达式
        Expression expression = parser.parseExpression(keyEl);
        //设置解析上下文(有哪些占位符，以及每种占位符的值)
        EvaluationContext context = new StandardEvaluationContext();
        if (fieldsName != null) {
            fieldsName.entrySet().forEach(entry -> context.setVariable(entry.getKey(), entry.getValue()));
        }

        //解析,获取替换后的结果
        String result = expression.getValue(context).toString();
        log.info("@MyLimit resultKey:{}", result);
        return result;

    }

    /**
     * 获取参数列表
     */
    private static Map<String, Object> getFieldsName(ProceedingJoinPoint joinPoint) {
        // 参数值
        Object[] args = joinPoint.getArgs();
        ParameterNameDiscoverer pnd = new DefaultParameterNameDiscoverer();
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        String[] parameterNames = pnd.getParameterNames(method);
        Map<String, Object> paramMap = new HashMap<>(32);
        for (int i = 0; i < parameterNames.length; i++) {
            paramMap.put(parameterNames[i], args[i]);
        }
        return paramMap;
    }

}
