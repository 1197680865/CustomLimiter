package com.example.customlimiter.cache;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.example.customlimiter.model.LimiterCategoryConfig;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

/**
 * @author ZhangChen
 * Created in 2024/1/9 20:06
 */
@Service
@Slf4j
public class RedisLimitChecker implements LimitChecker {

    public static final long NOT_EXIST_VALUE = -1;
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
    public boolean canDo(@Nonnull String limitKey, @Nonnull LimiterCategoryConfig config) {

        try {

            checkParam(limitKey, config);

            // 读取当前请求次数，读redis
            long currentCount = getLimitValue(limitKey);

            // 写入请求次数+1，并设置过期时间
            incrBy(limitKey, 1, config.getTtlInMillis());

            // 限流生效检查
            return canDoResult(currentCount, config.getLimit(), limitKey);

        } catch (Exception e) {

            log.error("canDo error, limitKey:{}:{}", limitKey, config, e);
            // 异常时 放行
            return true;
        }
    }

    private boolean canDoResult(long currentCount, long limit, String limitKey) {
        boolean canDo = currentCount == NOT_EXIST_VALUE || currentCount < limit;
        log.info("limitKey canDoResult:{},{}", limitKey, canDo);
        return canDo;
    }

    private void incrBy(@Nonnull String limitKey, long step, long expireMillis) {
        // 写入请求次数+1，并设置过期时间
        stringRedisTemplate.opsForValue().increment(limitKey, step);
        // 设置过期时间检查，过期后重新设置过期时间
        Long expire = stringRedisTemplate.getExpire(limitKey, TimeUnit.MILLISECONDS);
        if (expire == null || expire <= 0) {
            stringRedisTemplate.expire(limitKey, expireMillis, TimeUnit.MILLISECONDS);
        }

    }

    private long getLimitValue(String limitKey) {
        String value = stringRedisTemplate.opsForValue().get(limitKey);
        long currentCount = Optional.ofNullable(value).map(Long::parseLong)
                .orElse(NOT_EXIST_VALUE);
        log.info("limitKey:{}, currentCount:{}", limitKey, currentCount);
        return currentCount;
    }


    private void checkParam(String limitKey, LimiterCategoryConfig config) {
        if (StringUtils.isEmpty(limitKey)) {
            throw new IllegalArgumentException("limitKey is empty.");
        }
        if (config == null || !config.paramValid()) {
            throw new IllegalArgumentException("limit config is invalid.");
        }
    }

}
