package com.atguigu.gmall.passport;

import com.atguigu.gmall.util.HttpClientUtil;
import com.atguigu.gmall.webutil.util.GmallConstant;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GmallPassportWebApplicationTests {

    @Test
    public void contextLoads() {
        String s = HttpClientUtil.doGet("https://api.weibo.com/oauth2/authorize?client_id=" + GmallConstant.WEIBO_APP_KEY + "&response_type=code&redirect_uri=" + GmallConstant.REDIRECT_URL + "");
        System.out.println(s);
    }

}
