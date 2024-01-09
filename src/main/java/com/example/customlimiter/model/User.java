package com.example.customlimiter.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * @author ZhangChen
 * Created in 2024/1/7 20:15
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class User {

    private Long id;
    private String username;

}
