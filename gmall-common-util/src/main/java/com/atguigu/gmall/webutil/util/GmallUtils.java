package com.atguigu.gmall.webutil.util;


import org.apache.http.HttpResponse;
import java.text.SimpleDateFormat;
import java.util.*;

public class GmallUtils {

    /**
     * 返回文件扩展名
     * @param originalFileName
     * @return
     */

    public static String generateExtensionName(String originalFileName) {
        //扩展名
        String extensionName = "";
        if (originalFileName.contains(".")) {
            extensionName = originalFileName.substring(originalFileName.lastIndexOf(".") + 1);
        }
        return extensionName;
    }



    /**
     * 根据日期生成目录名称
     * @return
     */
    public static String generateForderNamebyData(String ossProjectParentFolder) {
        return ossProjectParentFolder+"/"+new SimpleDateFormat("yyyyMMdd").format(new Date());
    }



    /**
     * 生成用户登陆成功后使用的Token
     * @return
     */

    public static String generateToken() {

        return GmallConstant.REDIS_MEMBER_SING_TOKEN_PREFIX + UUID.randomUUID().toString().replaceAll("-", "");
    }

    /**
     * 验证集合是否有效
     * @param c		待验证集合
     * @return		验证结果（true：有效，false：无效）
     */
    public static <E> boolean collectionEffectiveCheck(Collection<E> c) {
        return (c != null) && (c.size() > 0);
    }

    /**
     * 验证字符串是否有效
     * @param source	待验证字符串
     * @return			验证结果（true：有效，false：无效）
     */
    public static boolean strEffectiveCheck(String source) {
        return (source != null) && (source.length() > 0);
    }

    /**
     * 生成随机验证码
     * @param length	验证码长度
     * @return			生成的验证码
     * @throws	RuntimeException 验证码长度必须大于0
     */
    public static String randomCode(int length) {

        if(length <= 0) {
            throw new RuntimeException(GmallConstant.MESSAGE_RANDOM_CODE_LENGTH_INVALID);
        }

        StringBuilder builder = new StringBuilder();

        for(int i = 0; i < length; i++) {

            // 1.生成随机数
            double doubleRandom = Math.random();

            // 2.调整
            int integerRandom = (int) (doubleRandom * 10);

            // 3.拼接
            builder.append(integerRandom);
        }

        return builder.toString();
    }

    /**
     * 发送验证码短信
     * @param appCode		阿里云市场中调用API时识别身份的appCode
     * @param randomCode	验证码值
     * @param phoneNum		接收验证码短信的手机号
     */
    public static void sendShortMessage(String appCode, String randomCode, String phoneNum) {

        String host = "http://dingxin.market.alicloudapi.com";
        String path = "/dx/sendSms";
        String method = "POST";

        Map<String, String> headers = new HashMap<String, String>();
        //最后在header中的格式(中间是英文空格)为Authorization:APPCODE 83359fd73fe94948385f570e3c139105
        headers.put("Authorization", "APPCODE " + appCode);
        Map<String, String> querys = new HashMap<String, String>();
        querys.put("mobile", phoneNum);
        querys.put("param", "code:"+randomCode);
        querys.put("tpl_id", "TP1711063");
        Map<String, String> bodys = new HashMap<String, String>();


        try {
            /**
             * 重要提示如下:
             * HttpUtils请从
             * https://github.com/aliyun/api-gateway-demo-sign-java/blob/master/src/main/java/com/aliyun/api/gateway/demo/util/HttpUtils.java
             * 下载
             *
             * 相应的依赖请参照
             * https://github.com/aliyun/api-gateway-demo-sign-java/blob/master/pom.xml
             */
            HttpResponse response = HttpUtils.doPost(host, path, method, headers, querys, bodys);
            System.out.println(response.toString());
            //获取response的body
            //System.out.println(EntityUtils.toString(response.getEntity()));
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

}