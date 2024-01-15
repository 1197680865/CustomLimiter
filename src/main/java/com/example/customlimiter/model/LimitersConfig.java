package com.example.customlimiter.model;

import java.util.HashMap;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author ZhangChen
 * Created in 2024/1/15 20:15
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LimitersConfig {
    private Map<String, LimiterCategoryConfig> configMap = new HashMap<>();
}
