package com.atguigu.gmall.util;

public class GmallConstant {

    public static final String REDIS_PROJECT_TEMP_TOKEN_PREFIX = "PROJECT_TEMP_TOKEN_";
    public static final String REDIS_RANDOM_CODE_PREFIX = "RANDOM_CODE_";
    public static final String REDIS_MEMBER_SING_TOKEN_PREFIX = "SIGNED_MEMBER_";

    public static final String MESSAGE_RANDOM_CODE_LENGTH_INVALID ="验证码长度不合法";
    public static final String MESSAGE_REDIS_KEY_OR_VALUE_INVALID ="待存入的Redis或Value不是有效字符串";
    public static final String MESSAGE_REDIS_TIME_OUT_INVALID ="参数错误，不接受0或null值，";
    public static final String MESSAGE_PHONE_NUM_INVALID ="手机号输入不正确";
    public static final String MESSAGE_LOGINACCT_INVALID ="登陆账号字符串无效";
    public static final String MESSAGE_CODE_NOT_MATCH ="验证码不匹配";
    public static final String MESSAGE_CODE_NOT_EXIST ="验证码不存在或已过期，请重新发送";
    public static final String MESSAGE_LOGINACCT_ALREADY_IN_USE ="用户名已经被占用";
    public static final String MESSAGE_LOGIN_FAILED="用户名或密码不正确";
    public static final String MESSAGE_ACCESS_DENIED ="请您先登录";

    public static final String ATTR_NAME_LOGIN_MEMBER ="LOGIN_MEMBER";
    public static final String ATTR_NAME_INIT_PROJECT ="INIT_PROJECT";

    public static final String MESSAGE_LOGIN_SUCCESS ="登陆成功";
    public static final String MESSAGE_REGISTER_FAILED ="注册失败";
    public static final String REQUEST_TIMEOUT ="请求超时,请稍后再试";

    public static final String MESSAGE_PHONE_NUM_ALREADY_EXIST ="手机号已存在，请直接登陆";
    public static final String MESSAGE_UPLOAD_FILE_EMPTY ="上传文件为空，请重新上传";

    public static final String IMG_URL ="http://192.168.0.107";
}
