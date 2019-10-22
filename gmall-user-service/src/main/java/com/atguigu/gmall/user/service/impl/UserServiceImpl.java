package com.atguigu.gmall.user.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.UmsMember;
import com.atguigu.gmall.bean.UmsMemberReceiveAddress;
import com.atguigu.gmall.service.UserService;
import com.atguigu.gmall.user.mapper.UmsMemberMapper;
import com.atguigu.gmall.user.mapper.UmsMemberReceiveAddressMapper;
import com.atguigu.gmall.user.mapper.UserMapper;
import com.atguigu.gmall.webutil.util.GmallUtils;
import com.atguigu.gmall.webutil.util.RedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UmsMemberReceiveAddressMapper umsMemberReceiveAddressMapper;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private UmsMemberMapper umsMemberMapper;


    @Override
    public List<UmsMember> getAll() {
        return userMapper.selectAll();
    }

    @Override
    public List<UmsMemberReceiveAddress> getReceiveAddressByMemberId(String memberId) {
        Example example = new Example(UmsMemberReceiveAddress.class);
        example.createCriteria().andEqualTo("memberId",memberId);
        return umsMemberReceiveAddressMapper.selectByExample(example);
    }

    @Override
    public UmsMember login(UmsMember umsMember) {
        Jedis jedis = null;
        try {
            jedis = redisUtil.getJedis();

            if(jedis!=null){
                String umsMemberStr = jedis.get("user:" + umsMember.getPassword()+umsMember.getUsername()+ ":info");

                if (StringUtils.isNotBlank(umsMemberStr)) {
                    // 密码正确
                    UmsMember umsMemberFromCache = JSON.parseObject(umsMemberStr, UmsMember.class);
                    return umsMemberFromCache;
                }
            }
            // 链接redis失败，开启数据库
            UmsMember umsMemberFromDb =loginFromDb(umsMember);
            if(umsMemberFromDb!=null){
                jedis.setex("user:" + umsMember.getPassword()+umsMember.getUsername()+ ":info",60*60*24, JSON.toJSONString(umsMemberFromDb));
            }
            return umsMemberFromDb;
        }finally {
            jedis.close();
        }

    }



    @Override
    public void addUserToken(String token, String memberId) {
        Jedis jedis = null;
        try {
          jedis = redisUtil.getJedis();
            jedis.setex("user:" + memberId + ":token", 60 * 60 * 2, token);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public UmsMember addOauthUser(UmsMember umsMember) {

        userMapper.insertSelective(umsMember);
        return umsMember;
    }

    @Override
    public UmsMember checkOauthMember(UmsMember umsMember) {
        Example example = new Example(UmsMember.class);
        example.createCriteria().andEqualTo("sourceUid", umsMember.getSourceUid());
        List<UmsMember> umsMembers = userMapper.selectByExample(example);
        if (GmallUtils.collectionEffectiveCheck(umsMembers)) {
            return umsMembers.get(0);
        }else {
            return null;
        }
    }

    @Override
    public void updateOuathMember(UmsMember umsMember) {
        Example example = new Example(UmsMember.class);
        example.createCriteria().andEqualTo("sourceUid", umsMember.getSourceUid());
        userMapper.updateByExample(umsMember, example);
    }

    @Override
    public UmsMemberReceiveAddress getReceiveAddressById(String receiveAddressId) {
        return umsMemberReceiveAddressMapper.selectByPrimaryKey(receiveAddressId);
    }

    private UmsMember loginFromDb(UmsMember umsMember) {
        Example example = new Example(UmsMember.class);
        example.createCriteria().andEqualTo("username", umsMember.getUsername())
                .andEqualTo("password", umsMember.getPassword());
        return umsMemberMapper.selectOneByExample(example);
    }


}
