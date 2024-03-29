package com.example.customlimiter;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.customlimiter.aspect.Limiter;
import com.example.customlimiter.model.User;

import lombok.extern.slf4j.Slf4j;

/**
 * @author ZhangChen
 * Created in 2024/1/7 20:46
 */
@RestController
@Slf4j
public class TestController {

    @PostMapping("/getUser")
    @Limiter(limitKey = "#user?.id")
    public Object getUser(@RequestBody User user){
        return "success";
    }

}
