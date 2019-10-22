package com.atguigu.gmall.order.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall.bean.OmsOrder;
import com.atguigu.gmall.order.mapper.OrderMapper;
import com.atguigu.gmall.service.OrderService;
import com.atguigu.gmall.webutil.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

import java.util.Collections;
import java.util.UUID;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private OrderMapper orderMapper;


    @Override
    public String generateTradeCode(String memberId) {
        Jedis jedis = null;
        String tradeCode = "";
        try {
            jedis= redisUtil.getJedis();
            String tradeKey = "user:" + memberId + ":tradeCode";
            tradeCode=UUID.randomUUID().toString();
            jedis.setex(tradeKey, 60 * 10, tradeCode);

        } catch (Exception e) {

            e.printStackTrace();
        }

        jedis.close();

        return tradeCode;
    }

    @Override
    public String checkTradeCode(String memberId,String tradeCode) {
        Jedis jedis = null;
        String tradeKey = "user:" + memberId + ":tradeCode";
        try {
            jedis = redisUtil.getJedis();
            String tradeCodeFromCache = jedis.get(tradeKey);

                // 使用lua脚本在发现key的同时将key删除，防止并发订单攻击
                String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
                Long eval = (Long) jedis.eval(script, Collections.singletonList(tradeKey), Collections.singletonList(tradeCode));
                if (eval!=null&&eval!=0) {
                    return "success";
                } else {
                    return "fail";
                }
        } catch (Exception e){
            e.printStackTrace();
        }finally {
            jedis.close();

        }
        return "fail";
    }

    @Override
    public void saveOrder(OmsOrder omsOrder) {
        orderMapper.insertSelective(omsOrder);
    }
}
