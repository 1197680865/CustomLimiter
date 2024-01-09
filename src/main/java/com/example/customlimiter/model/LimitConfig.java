package com.example.customlimiter.model;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author ZhangChen
 * Created in 2024/1/9 20:15
 */
@Data
@AllArgsConstructor
public class LimitConfig {

    private int limit;
    private int ttlInMillis;

    public boolean paramValid() {
        return limit > 0 && ttlInMillis > 0;
    }
}
