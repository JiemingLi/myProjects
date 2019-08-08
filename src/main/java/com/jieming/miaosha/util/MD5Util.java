package com.jieming.miaosha.util;

import org.apache.commons.codec.digest.DigestUtils;

public class MD5Util {

    private static final String salt = "1a2b3c4d";


    public static String md5(String src) {
        return DigestUtils.md5Hex(src);
    }

    public static String md5Util(String src){
        return DigestUtils.md5Hex(src);
    }

    /*浏览器输入的密码进行一次加盐的md5加密*/
    public static String inputPassToForm(String inputPass){
        String temp = "" + salt.charAt(0) + salt.charAt(2) + inputPass + salt.charAt(5) + salt.charAt(4);
        return md5Util(temp);
    }

    public static String formPassToDB(String formPass,String salt){
        String temp = "" + salt.charAt(0) + salt.charAt(2) + formPass + salt.charAt(5) + salt.charAt(4);
        return md5Util(temp);
    }

    public static String inputPassToDB(String input,String saltDB){
        String formPass = inputPassToForm(input);
        String dbPass = formPassToDB(formPass,saltDB);
        return dbPass;
    }

    public static void main(String[] args) {
        System.out.println(inputPassToDB("123456",salt));
        //b7797cce01b4b131b433b6acf4add449
    }
}
