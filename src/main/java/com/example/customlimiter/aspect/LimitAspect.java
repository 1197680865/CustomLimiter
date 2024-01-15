package com.example.customlimiter.aspect;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.example.customlimiter.cache.LimitChecker;
import com.example.customlimiter.model.LimiterCategoryConfig;
import com.example.customlimiter.model.LimitersConfig;

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

    @Resource
    private LimitersConfig limitersConfig;


    /**
     * 对加有@Limiter的方法生效
     */
    @Before("@annotation(com.example.customlimiter.aspect.Limiter)")
    public void limitBefore(JoinPoint point) {

        boolean pass = true;
        try {

            pass = check(point);

        } catch (Exception e) {

            log.warn("LimitAspect Exception. point:{}", point, e);
        }

        if (!pass) {
            throw new RuntimeException("limit block.");
        }
    }


    private boolean check(JoinPoint point) {

        // 获取URI
        ServletRequestAttributes requestAttr =
                (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        String requestUri = requestAttr.getRequest().getRequestURI();

        // 获取注解对象
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();
        Limiter limiterAnnotation = method.getAnnotation(Limiter.class);

        //获取到请求参数Map
        Map<String, Object> fieldsName = getFieldsName(point);

        // 生成限流的key
        String limitKey = generateKey(limiterAnnotation, fieldsName);

        // 读取限流配置
        String limitCategory = requestUri;
        LimiterCategoryConfig config = limitersConfig.getConfigMap().get(limitCategory);

        // 限流判断
        boolean canDo = redisLimitChecker.canDo(limitKey, config);

        if (canDo) {
            // 放行
            log.info("limit pass. limitKey:{}, limitCategory:{}", limitKey, limitCategory);
        } else {
            log.error("limit block. limitKey:{}, limitCategory:{}", limitKey, limitCategory);
        }

        return canDo;

    }


    private String generateKey(Limiter limiterAnnotation, Map<String, Object> fieldsName) {
        //获取注解上的值如 :  @MyLimit(limitKey = "#user?.id")
        String keyEl = limiterAnnotation.limitKey();
        if (StringUtils.isEmpty(keyEl)) {
            throw new IllegalArgumentException("limiterAnnotation limitKey is empty");
        }

        //创建解析器
        SpelExpressionParser parser = new SpelExpressionParser();
        //获取表达式
        Expression expression = parser.parseExpression(keyEl);
        //设置解析上下文(有哪些占位符，以及每种占位符的值)
        EvaluationContext context = new StandardEvaluationContext();
        if (fieldsName != null) {
            fieldsName.forEach(context::setVariable);
        }

        //解析,获取替换后的结果
        Object value = expression.getValue(context);
        if (value == null) {
            throw new IllegalArgumentException("limiterAnnotation expression.getValue is null.");
        }

        log.info("@Limit resultKey:{}", value);
        return value.toString();

    }

    /**
     * 获取参数列表
     */
    private static Map<String, Object> getFieldsName(JoinPoint joinPoint) {
        // 参数值数组
        Object[] args = joinPoint.getArgs();
        ParameterNameDiscoverer pnd = new DefaultParameterNameDiscoverer();
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        // 参数名数组
        String[] parameterNames = pnd.getParameterNames(method);

        Map<String, Object> paramMap = new HashMap<>(32);
        if (parameterNames != null) {
            for (int i = 0; i < parameterNames.length; i++) {
                paramMap.put(parameterNames[i], args[i]);
            }
        }
        return paramMap;
    }


    //
    //    /**
    //     * 对加有@RestController类下的所有方法生效
    //     */
    //    @Around("@within(org.springframework.web.bind.annotation.RestController)")
    //    public Object aroundRestControllerAnno(ProceedingJoinPoint point) throws Throwable {
    //        return process(point);
    //    }


}
