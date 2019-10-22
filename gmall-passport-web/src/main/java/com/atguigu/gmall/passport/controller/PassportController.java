package com.atguigu.gmall.passport.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.UmsMember;
import com.atguigu.gmall.service.UserService;
import com.atguigu.gmall.util.HttpClientUtil;
import com.atguigu.gmall.webutil.util.GmallConstant;
import com.atguigu.gmall.webutil.util.GmallUtils;
import com.atguigu.gmall.webutil.util.JwtUtil;
import com.atguigu.gmall.webutil.util.MD5Util;
import jdk.nashorn.internal.parser.Token;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Controller
public class PassportController {

    @Reference
    UserService userService;

    /**
     * 调用微博登陆接口进行登录
     * @param code
     * @param request
     * @return
     */
    @RequestMapping("vlogin")
    public String vlogin(String code,HttpServletRequest request,ModelMap modelMap){
        //code授权码换取Access_token
        String accessTokenUrl = GmallConstant.WEIBO_ACCESS_TOKEN;
        Map<String, String> map = new HashMap<>();
        map.put("client_id", GmallConstant.WEIBO_APP_KEY);
        map.put("client_secret", GmallConstant.WEIBO_ACCESS_SECRET);
        map.put("grant_type", "authorization_code");
        map.put("redirect_uri", GmallConstant.REDIRECT_URL);
        map.put("code",code);

        String accessTokenJson = HttpClientUtil.doPost(accessTokenUrl, map);
        Map<String, String> access_map=JSON.parseObject(accessTokenJson, Map.class);


        //access_token 换取用户信息
        String access_token = access_map.get("access_token");
        String uid = access_map.get("uid");

        String userInfoUrl = GmallConstant.WEIBO_USER_SHOWS_URL + "?access_token=" + access_token + "&uid=" + uid;

        String userJson = HttpClientUtil.doGet(userInfoUrl);

        Map<String,Object> userInfoMap = JSON.parseObject(userJson, Map.class);
        String iconPath = (String) userInfoMap.get("avatar_hd");
        String name = (String) userInfoMap.get("screen_name");
        String city = (String) userInfoMap.get("location");
        String gender = (String) userInfoMap.get("gender");
        if (gender.equals("m")) {
            gender = "1";
        } else if (gender.equals("f")) {
            gender = "2";
        } else {
            gender = "0";
        }

        //将用户数据保存到数据库
        UmsMember umsMember = new UmsMember();
        umsMember.setNickname(name);
        umsMember.setIcon(iconPath);
        umsMember.setSourceType("1");
        umsMember.setAccessCode(code);
        umsMember.setAccessToken(access_token);
        umsMember.setSourceUid(uid);
        umsMember.setCity(city);
        umsMember.setGender(gender);
        umsMember.setStatus(1);
        umsMember.setMemberLevelId("1");
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String date = simpleDateFormat.format(new Date());
        umsMember.setCreateTime(date);

        UmsMember oauthMember= userService.checkOauthMember(umsMember);
        if (oauthMember != null) {
            umsMember = oauthMember;
            userService.updateOuathMember(umsMember);
        } else {
            umsMember = userService.addOauthUser(umsMember);

        }

        //生成jwt的token，重定向到首页，携带该token
        String memberId = umsMember.getId();
        String nickname = umsMember.getNickname();


        String token = "";
        String ip = "";
        ip = getIpByNginx(request);
        token = makeToken(memberId, nickname,ip);

        // 将token存入redis一份
        userService.addUserToken(token,memberId);
        modelMap.put("nickname", nickname);

        return "redirect:http://search.gmall.com:8083?token="+token;
    }




    @RequestMapping("verify")
    @ResponseBody
    public String verify(String token, String currentIp){

        // 通过jwt校验token真假
        Map<String,String> map = new HashMap<>();

        Map<String, Object> decode = JwtUtil.decode(token, "2019gmall0105", currentIp);

        if(decode!=null){
            map.put("status","success");
            map.put("memberId",(String)decode.get("memberId"));
            map.put("nickname",(String)decode.get("nickname"));
        }else{
            map.put("status","fail");
        }
        return JSON.toJSONString(map);
    }



    @RequestMapping("login")
    @ResponseBody
    public String login(UmsMember umsMember, HttpServletRequest request){

        String token = "";
        String ip = "";
        String password = umsMember.getPassword();
        // 调用用户服务验证用户名和密码
        umsMember.setPassword(MD5Util.digest(password));

        UmsMember umsMemberLogin = userService.login(umsMember);

        // 登录成功
        if(umsMemberLogin!=null){
            String memberId = umsMemberLogin.getId();
            String nickname = umsMemberLogin.getNickname();
            ip = getIpByNginx(request);
            //取回token
            token = makeToken(memberId, nickname, ip);
            // 将token存入redis一份
            userService.addUserToken(token,memberId);
            request.getSession().setAttribute("memberId", memberId);

        }else{
            // 登录失败
            token = "fail";
        }
        return token;
    }

    /**
     * 获取请求的Ip
     * @param request
     * @return
     */
    private String getIpByNginx(HttpServletRequest request) {

        // 通过nginx转发的客户端ip
        String ip = request.getHeader("x-forwarded-for");
        if(StringUtils.isBlank(ip)){
            // 从request中获取ip
            ip = request.getRemoteAddr();
            if(StringUtils.isBlank(ip)){
                ip = "127.0.0.1";
            }
        }
        return ip;
    }

    /**
     * jwt制作token
     * @param memberId
     * @param nickname
     * @param ip
     * @return
     */
    private String makeToken(String memberId,String nickname,String ip) {
        String token = "";
        Map<String,Object> userMap = new HashMap<>();
        userMap.put("memberId",memberId);
        userMap.put("nickname",nickname);
        // 按照设计的算法对参数进行加密后，生成token
        token = JwtUtil.encode("2019gmall0105", userMap, ip);

        return token;
    }

    @RequestMapping("index")
    public String index(String ReturnUrl, ModelMap map){
        map.put("ReturnUrl",ReturnUrl);
        map.put("SinaWeiBoUrl", GmallConstant.SINA_WEIBO_URL);

        return "index";
    }




}
