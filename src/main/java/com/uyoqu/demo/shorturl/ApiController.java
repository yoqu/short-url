package com.uyoqu.demo.shorturl;

import com.uyoqu.demo.shorturl.utils.Md5Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.util.Objects;
import java.util.Set;

@RestController
public class ApiController {

    public static final String RANK_KEY = "ranks";
    public static final String CODE_KEY = "values";
    @Autowired
    StringRedisTemplate redisTemplate;
    @Autowired
    private ConfigProperties config;

    @GetMapping("generate")
    public String generate(String longUrl, @RequestParam(required = false, name = "shortCode") String shortCode) {
        if (StringUtils.isEmpty(longUrl)) {
            return "error:长连接不能为空";
        }
        char[] charData = config.getCharsetData().toCharArray();
        String md5 = Md5Utils.to32LowerCase(longUrl);
        BoundHashOperations hashOperations = redisTemplate.boundHashOps(CODE_KEY);
        if (StringUtils.isEmpty(shortCode)) {
            //1. 取出32位md5值
            int i = 0;
            //2. 截断md5的长度
            int size = md5.length() % config.getLength() == 0 ? md5.length() / config.getLength() : md5.length() / config.getLength() + 1;
            char[] codeChars = new char[config.getLength()];
            for (int j = 0; j < config.getLength(); j++) {
                //3. 把截断的md5值转换为16进制数据并从字符集中取余得到字符下标索引
                Long index = Long.parseLong(md5.substring(i, i + size >= md5.length() ? md5.length() : i + size), 16) % Integer.valueOf(charData.length).longValue();
                i = i + size;
                codeChars[j] = charData[index.intValue()];
            }
            shortCode = new String(codeChars);
            Boolean result = hashOperations.putIfAbsent(shortCode, longUrl);
            if (!result) {
                String dbLongUrl = (String) hashOperations.get(shortCode);
                //4. 如果不同的长连接因为碰撞产生在一起直接报错，让用户重试
                if (!Objects.equals(dbLongUrl, longUrl)) {
                    //5. 极端情况下算出来的短码相同但长连接还不同那就最后一位取全额md5值计算一次，还失败直接结束
                    codeChars = shortCode.toCharArray();
                    Long index = Long.parseLong(md5, 16) % Integer.valueOf(charData.length).longValue();
                    codeChars[config.getLength() - 1] = charData[index.intValue()];
                    if (!Objects.equals(new String(codeChars), shortCode)) {
                        shortCode = new String(codeChars);
                        result = hashOperations.putIfAbsent(shortCode, longUrl);
                        if (result) {
                            return "suc:" + config.getBaseUrl() + shortCode;
                        } else {
                            dbLongUrl = (String) hashOperations.get(shortCode);
                            if (Objects.equals(dbLongUrl, longUrl)) {
                                return "suc:" + config.getBaseUrl() + shortCode;
                            }
                        }
                    }
                    return "error:生成的短码已被占用，请重试";
                }
            }
            return "suc:" + config.getBaseUrl() + shortCode;
        } else {
            if (shortCode.length() > config.getCustomerMaxLength()) {
                return "error:自定义短连接过长,最大长度" + config.getCustomerMaxLength();
            }
            Boolean result = hashOperations.putIfAbsent(shortCode, longUrl);
            if (!result) {
                String dbLongUrl = (String) hashOperations.get(shortCode);
                //4. 如果不同的长连接因为碰撞产生在一起直接报错，让用户重试
                if (!Objects.equals(dbLongUrl, longUrl)) {
                    return "error:您定义的短码已被占用，请换一个吧";
                }
            }
            return "suc:" + config.getBaseUrl() + shortCode;
        }
    }

    @GetMapping("link/{shortCode}")
    public void redirect(@PathVariable("shortCode") String shortCode, HttpServletResponse httpServletResponse) {
        BoundHashOperations hashOperations = redisTemplate.boundHashOps(CODE_KEY);
        String longUrl = (String) hashOperations.get(shortCode);
        if (StringUtils.isEmpty(longUrl)) {
            return;
        } else {
            redisTemplate.boundZSetOps(RANK_KEY).incrementScore(shortCode, 1);
            httpServletResponse.setStatus(302);
            httpServletResponse.setHeader("Location", longUrl);
            return;
        }
    }

    @GetMapping("/ranks")
    public Set<ZSetOperations.TypedTuple<String>> rank() {
        Set<ZSetOperations.TypedTuple<String>> ranks = redisTemplate.boundZSetOps(RANK_KEY).reverseRangeWithScores(0, -1);
        return ranks;
    }
}
