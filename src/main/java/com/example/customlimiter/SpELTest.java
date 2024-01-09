package com.example.customlimiter;

import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import com.example.customlimiter.model.User;

/**
 * @author ZhangChen
 * Created in 2024/1/7 20:15
 */
public class SpELTest {

    public static void main(String[] args) {
        testSPelObject();

    }

    private static void testSPelObject(){
        User user = new User(2L, "ZhangSan");
        // SPEL 表达式：获取对象参数中的属性
        String keyEl = "'param is ' + #user?.id";
        //创建解析器
        SpelExpressionParser parser = new SpelExpressionParser();
        //获取表达式
        Expression expression = parser.parseExpression(keyEl);
        //设置解析上下文(有哪些占位符，以及每种占位符的值)
        EvaluationContext context = new StandardEvaluationContext();
        context.setVariable("user", user);
        //解析,获取替换后的结果
        String result = expression.getValue(context).toString();

        System.out.println(result);
    }
}
