package com.uyoqu.demo.shorturl;

import cn.hutool.http.HttpUtil;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ShortUrlApplicationTests {

    @Autowired
    ApiController apiController;

    /**
     * 自动生成测试
     */
    @Test
    public void testAutoGen() {
        String longUrl = "http://www.baidu.com";
        String resp = apiController.generate(longUrl, null);
        Assert.assertFalse(resp.startsWith("error:"));
        String data = resp.substring(4);
        String location = HttpUtil.createGet(data).execute().header("Location");
        Assert.assertEquals(longUrl, location);
    }
    /**
     * 手动生成短码测试
     */
    @Test
    public void testGen() {
        String longUrl = "http://www.baidu.com";
        String resp = apiController.generate(longUrl, "abc");
        Assert.assertFalse(resp.startsWith("error:"));
        String data = resp.substring(4);
        String location = HttpUtil.createGet(data).execute().header("Location");
        Assert.assertEquals(longUrl, location);
    }
}
