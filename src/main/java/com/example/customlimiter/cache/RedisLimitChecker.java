package com.example.customlimiter.cache;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.example.customlimiter.model.LimitConfig;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

/**
 * @author ZhangChen
 * Created in 2024/1/9 20:06
 */
@Service
@Slf4j
public class RedisLimitChecker implements LimitChecker {


    private Map<String, LimitConfig> limitConfigMap;
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 1.根据key读取请求次数。 value存在；value不存在说明请求次数为0
     * 2.写入请求次数+1（当key不存在时会创建kv，当key存在时，v+1）
     * 3.检查过期时间，过期了则更新过期时间
     * （单独设置过期时间，避免并发赋值+过期时间导致计数值被覆盖的问题。但无法避免过期时间并发修改，但此问题可以容忍）
     * 4.如果请求次数大于限制次数，则返回false，否则返回true
     */
    @Override
    public boolean canDo(String limitKey, String category) {

        String integer = stringRedisTemplate.opsForValue().get(limitKey);
        // 读取当前请求次数，读redis
        int currentQueryCount = Optional.ofNullable(integer).map(Integer::parseInt).orElse(0);
        log.info("limitKey:{}, currentQueryCount:{}", limitKey, currentQueryCount);

        // 写入请求次数+1，并设置过期时间
        stringRedisTemplate.opsForValue().increment(limitKey);

        // 读取当前category 对应的限制配置
        LimitConfig limitConfig = limitConfigMap.get(category);
        if (limitConfig ==null || !limitConfig.paramValid()){
            return true;
        }

        // 设置过期时间检查，过期后重新设置过期时间
        Long expire = stringRedisTemplate.getExpire(limitKey, TimeUnit.MILLISECONDS);
        if (expire == null || expire <= 0) {
            stringRedisTemplate.expire(limitKey, limitConfig.getTtlInMillis(), TimeUnit.MILLISECONDS);
        }

        // 限流生效检查
        if (currentQueryCount >= limitConfig.getLimit()){
            return false;
        }

        return true;
    }

    @PostConstruct
    public void mockConfig(){
        limitConfigMap = new HashMap<>();

        // 10分钟只能通过一个请求
        limitConfigMap.put("/getUser", new LimitConfig(1,60_0000));

    }
}
