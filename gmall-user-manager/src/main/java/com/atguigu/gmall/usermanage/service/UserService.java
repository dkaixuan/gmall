package com.atguigu.gmall.usermanage.service;

import com.atguigu.gmall.usermanage.bean.UmsMember;
import com.atguigu.gmall.usermanage.bean.UmsMemberReceiveAddress;

import java.util.List;

public interface UserService {
    List<UmsMember> getAll();

    List<UmsMemberReceiveAddress> getReceiveAddressByMemberId(String memberId);
}
