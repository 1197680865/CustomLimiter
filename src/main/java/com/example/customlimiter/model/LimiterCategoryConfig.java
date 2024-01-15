package com.example.customlimiter.model;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author ZhangChen
 * Created in 2024/1/9 20:15
 */
@Data
@AllArgsConstructor
public class LimiterCategoryConfig {

    /**
     * 单位时间的请求个数最大值
     */
    private long limit;
    /**
     * 单位时间（单位毫秒）
     */
    private int ttlInMillis;

    public boolean paramValid() {
        return limit > 0 && ttlInMillis > 0;
    }
}
