package com.atguigu.gmall.cart.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.OmsCartItem;
import com.atguigu.gmall.cart.mapper.OmsCartItemMapper;
import com.atguigu.gmall.service.CartService;
import com.atguigu.gmall.webutil.util.RedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CartServiceImpl implements CartService{

    @Autowired
    private OmsCartItemMapper omsCartItemMapper;

    @Autowired
    private RedisUtil redisUtil;


    @Override
    public OmsCartItem ifCartExistByUser(String memberId,String skuId) {
        OmsCartItem omsCartItem = new OmsCartItem();
        omsCartItem.setMemberId(memberId);
        omsCartItem.setProductSkuId(skuId);
        return omsCartItemMapper.selectOne(omsCartItem);
    }

    @Override
    public void addCart(OmsCartItem omsCartItem) {
        if (StringUtils.isNotBlank(omsCartItem.getMemberId())) {
            omsCartItemMapper.insertSelective(omsCartItem);
        }

    }

    @Override
    public void updateCart(OmsCartItem omsCartItemFromDb) {
        Example example = new Example(OmsCartItem.class);
        example.createCriteria().andEqualTo("id", omsCartItemFromDb.getId());
        omsCartItemMapper.updateByExampleSelective(omsCartItemFromDb, example);
    }

    @Override
    public void flushCartCache(String memberId) {
        OmsCartItem omsCartItem = new OmsCartItem();
        omsCartItem.setMemberId(memberId);
        List<OmsCartItem> omsCartItemList = omsCartItemMapper.select(omsCartItem);

        // 同步到redis缓存中
        Jedis jedis = redisUtil.getJedis();

        Map<String, String> map = new HashMap<>();
        for (OmsCartItem cartItem : omsCartItemList) {
            cartItem.setTotalPrice(cartItem.getPrice().multiply(cartItem.getQuantity()));
            map.put(cartItem.getProductSkuId(), JSON.toJSONString(cartItem));
        }
        jedis.del("user:"+memberId+":cart");
        jedis.hmset("user:"+memberId +":cart", map);
        jedis.close();
    }

    @Override
    public List<OmsCartItem> cartList(String memberId) {
        List<OmsCartItem> omsCartItemList = new ArrayList<>();
        Jedis jedis = null;
        try {
            jedis = redisUtil.getJedis();
            List<String> cartList = jedis.hvals("user:"+memberId+":cart");
            for (String hval : cartList) {
                OmsCartItem omsCartItem = JSON.parseObject(hval, OmsCartItem.class);
                omsCartItemList.add(omsCartItem);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            jedis.close();
        }
        return omsCartItemList;
    }

    @Override
    public void checkCart(OmsCartItem omsCartItem) {
        //刷新DB
        Example example = new Example(OmsCartItem.class);
        String memberId=omsCartItem.getMemberId();
        String productId = omsCartItem.getProductSkuId();

        example.createCriteria().andEqualTo("memberId",memberId).andEqualTo("productSkuId",omsCartItem.getProductSkuId());
        //刷新redis
        Jedis jedis = null;
        Map<String, String> map = new HashMap<>();
        try {
            jedis = redisUtil.getJedis();
            List<String> cartList = jedis.hvals("user:"+memberId+":cart");
            for (String hval : cartList) {
                OmsCartItem cartItem = JSON.parseObject(hval, OmsCartItem.class);
                if (cartItem.getProductSkuId().equals(productId)) {
                    cartItem.setIsChecked(omsCartItem.getIsChecked());
                }
                map.put(cartItem.getProductSkuId(), JSON.toJSONString(cartItem));
            }
            jedis.del("user:"+memberId+":cart");
            jedis.hmset("user:"+memberId +":cart", map);

        } catch (Exception e) {
            e.printStackTrace();
        }
        omsCartItemMapper.updateByExampleSelective(omsCartItem,example);
    }


}
