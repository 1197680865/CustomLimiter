package com.example.customlimiter.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.customlimiter.model.LimiterCategoryConfig;
import com.example.customlimiter.model.LimitersConfig;

/**
 * @author ZhangChen
 * Created in 2024/1/15 20:14
 */
@Configuration
public class MockConfigCenter {

    @Bean
    public LimitersConfig limitersConfig() {
        Map<String, LimiterCategoryConfig> limitConfigMap = new HashMap<>();
        // 1分钟只能通过一个请求
        limitConfigMap.put("/getUser", new LimiterCategoryConfig(1, 60_000));
        return new LimitersConfig(limitConfigMap);
    }

}
