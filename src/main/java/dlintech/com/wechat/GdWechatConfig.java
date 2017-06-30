package dlintech.com.wechat;

/**
 * Created by zgdcool on 16/8/23.
 *
 */
public class GdWechatConfig {
    //微信appid
    public static String appid;
    //微信secret
    public static String secret;
    //微信商户号
    public static String partner;
    //微信商户支付密钥
    public static String partnerkey;
    //微盟appid
    public static String weimengAppid;
    //微盟secret
    public static String weimengSecret;

    public static String getAppid() {
        return appid;
    }

    public static void setAppid(String appid) {
        GdWechatConfig.appid = appid;
    }

    public static String getSecret() {
        return secret;
    }

    public static void setSecret(String secret) {
        GdWechatConfig.secret = secret;
    }

    public static String getPartner() {
        return partner;
    }

    public static void setPartner(String partner) {
        GdWechatConfig.partner = partner;
    }

    public static String getPartnerkey() {
        return partnerkey;
    }

    public static void setPartnerkey(String partnerkey) {
        GdWechatConfig.partnerkey = partnerkey;
    }

    public static String getWeimengAppid() {
        return weimengAppid;
    }

    public static void setWeimengAppid(String weimengAppid) {
        GdWechatConfig.weimengAppid = weimengAppid;
    }

    public static String getWeimengSecret() {
        return weimengSecret;
    }

    public static void setWeimengSecret(String weimengSecret) {
        GdWechatConfig.weimengSecret = weimengSecret;
    }
}
