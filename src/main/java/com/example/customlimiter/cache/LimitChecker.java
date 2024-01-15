package com.example.customlimiter.cache;

import com.example.customlimiter.model.LimiterCategoryConfig;

/**
 * @author ZhangChen
 * Created in 2024/1/9 20:03
 */
public interface LimitChecker {

    boolean canDo(String limitKey, LimiterCategoryConfig config);
}
