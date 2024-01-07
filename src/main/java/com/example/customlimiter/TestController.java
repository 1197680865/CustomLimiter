package com.example.customlimiter;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

/**
 * @author ZhangChen
 * Created in 2024/1/7 20:46
 */
@RestController
@Slf4j
public class TestController {

    @PostMapping("/getUser")
    @MyLimit(limitKey = "#user?.id")
    public Object getUser(@RequestBody User user){


        return "success";

    }

}
