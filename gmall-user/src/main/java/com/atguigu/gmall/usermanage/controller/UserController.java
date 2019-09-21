package com.atguigu.gmall.usermanage.controller;

import com.atguigu.gmall.bean.UmsMember;
import com.atguigu.gmall.bean.UmsMemberReceiveAddress;
import com.atguigu.gmall.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class UserController {

    @Autowired
    private UserService userService;




    @RequestMapping("/index")
    public List<UmsMember> index() {

        List<UmsMember> list = userService.getAll();

        return list;
    }


    @RequestMapping("/getAddress")
    public List<UmsMemberReceiveAddress> getReceiveAddressByMemberId(String memberId) {
        List<UmsMemberReceiveAddress> list = userService.getReceiveAddressByMemberId(memberId);
        return list;
    }





}
