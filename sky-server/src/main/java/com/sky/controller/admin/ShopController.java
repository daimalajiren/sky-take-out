package com.sky.controller.admin;

import com.sky.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import static com.sky.controller.user.ShopController.KEY;

@RestController("adminShopController")//为避免spring里的bean重名导致项目编译失败，这里使用注解指定bean名
@RequestMapping("/admin/shop")
@Slf4j
public class ShopController {
    @Autowired
    private RedisTemplate redisTemplate;
    /**
     * 设置营业状态
     * @param status
     * @return
     */

    @PutMapping("/{status}")
    public Result setStatus(@PathVariable Integer status)
    {
        log.info("设置店铺营业状态:{}", status == 1 ? "营业中":"打烊");
        redisTemplate.opsForValue().set(KEY, status);
        return Result.success();
    }

    /**
     * 获取营业状态
     * @return
     */
    @GetMapping("/status")
    public Result<Integer> getStatus()
        {

            Integer status = (Integer) redisTemplate.opsForValue().get(KEY);
            log.info("店铺营业状态:{}", status == 1 ? "营业中":"打烊");
            return Result.success(status);
        }
}
