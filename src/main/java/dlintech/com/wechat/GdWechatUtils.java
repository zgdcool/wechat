package dlintech.com.wechat;

import dlintech.com.tools.GdDateTimeUtils;
import dlintech.com.tools.GdNumberUtils;
import dlintech.com.tools.gson.GdJsonMapper;
import dlintech.com.tools.http.GDHttpClient;
import dlintech.com.tools.security.GdSha1Util;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import static java.lang.Integer.parseInt;

/**
 * Created by zgdcool on 16/8/23.
 *
 */
public class GdWechatUtils {
    /**
     * 组装获取code的url(微信)
     *
     * @param backUri
     * @param scope
     * @return
     */
    @SuppressWarnings("deprecation")
    public static String getWeChatCode(String backUri, GdWechatScopeTypeEnum scope) throws UnsupportedEncodingException {
        backUri = URLEncoder.encode(backUri, "UTF-8");
        String url = "https://open.weixin.qq.com/connect/oauth2/authorize?appid=" + GdWechatConfig.appid +
                "&redirect_uri=" + backUri + "&response_type=code&scope=" + scope.getTypeCode() + "&state=123#wechat_redirect";
        return url;

    }

    /**
     * 组装获取code的url(微盟)
     *
     * @param backUri
     * @param scope
     * @return
     */
    @SuppressWarnings("deprecation")
    public static String getWeiMengCode(String backUri, String scope) throws UnsupportedEncodingException {
        backUri = URLEncoder.encode(backUri, "UTF-8");
        String url = "https://open.weimob.com/oauth2/openid/authorize?"
                + "client_id=" + GdWechatConfig.weimengAppid
                + "&redirect_uri=" + backUri
                + "&response_type=code"
                + "&scope=" + scope + "&state=123#wechat_redirect";
        return url;

    }

    /**
     * 获取openID和token(微盟)
     *
     * @param code
     * @return
     */
    public static Map<Object, Object> getOpenIdAndTokenByCodeWeiMeng(String code) throws IOException {
        //微盟
        String URL = "https://open.weimob.com/oauth2/openid/access_token";
        if (StringUtils.isNotEmpty(code)) {
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("client_id", GdWechatConfig.weimengAppid);
            params.put("client_secret", GdWechatConfig.weimengSecret);
            params.put("code", code);
            params.put("grant_type", "authorization_code");
            String resultStr = GDHttpClient.post(URL, params);
            if (StringUtils.isNotEmpty(resultStr)) {
                Map<Object, Object> map = GdJsonMapper.readStringValueToMap(resultStr);
                return map;
            }
        }
        return null;
    }

    public static Map<Object, Object> getOpenIdAndTokenByCodeWechat(String code) {
        //微信
        String URL = "https://api.weixin.qq.com/sns/oauth2/access_token?appid=" + GdWechatConfig.appid
                + "&secret=" + GdWechatConfig.secret + "&code=" + code + "&grant_type=authorization_code";
        if (StringUtils.isNotEmpty(code)) {
            String resultStr = GDHttpClient.get(URL);
            if (StringUtils.isNotEmpty(resultStr)) {
                Map<Object, Object> map = GdJsonMapper.readStringValueToMap(resultStr);
                return map;
            }
        }
        return null;
    }

    /**
     * 获取微信用户信息(微信)
     *
     * @param openId
     * @param token
     * @return
     * @throws Exception
     */
    public static Map<Object, Object> getUserInfoWechat(String openId, String token) throws Exception {
        String userinfourl = "https://api.weixin.qq.com/sns/userinfo?access_token=" + token + "&openid=" + openId + "&lang=zh_CN";
        String resultStr = GDHttpClient.get(userinfourl);
        if (StringUtils.isNotEmpty(resultStr)) {
            return (Map<Object, Object>) GdJsonMapper.readStringValueToMap(resultStr);
        }
        return null;
    }

    /**
     * 获取微信用户信息(微盟)
     *
     * @param openId
     * @param token
     * @return
     * @throws Exception
     */
    public static Map<Object, Object> getUserInfoWeiMeng(String openId, String token) throws Exception {
        String userinfourl = "https://open.weimob.com/oauth2/openid/userinfo?access_token=" + token + "&openid=" + openId + "&lang=zh_CN";
        String resultStr = GDHttpClient.get(userinfourl);
        if (StringUtils.isNotEmpty(resultStr)) {
            return GdJsonMapper.readStringValueToMap(resultStr);
        }
        return null;
    }

    /**
     * 获取支付结果
     *
     * @return
     * @throws Exception
     */
    public static Map<String, String> getPayResult(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Map<String, String> map = new HashMap();
        BufferedReader br = new BufferedReader(new InputStreamReader(request.getInputStream()));
        String line;
        StringBuilder sb = new StringBuilder();
        while ((line = br.readLine()) != null) {
            sb.append(line);
        }
        System.out.println("-------------weixincanshu--------------" + sb.toString());
        Map resultMap = GdGetWxOrderno.doXMLParse(sb.toString());
        GdRequestHandler reqHandler = new GdRequestHandler(request, response);
        reqHandler.init(GdWechatConfig.appid, GdWechatConfig.secret, GdWechatConfig.partnerkey);
        if (!reqHandler.checkSign(resultMap)) {
            map.put("return_code", "FAIL");
            return map;
        }
        String return_code = (String) resultMap.get("return_code");
        String result_code = (String) resultMap.get("result_code");
        if ("SUCCESS".equals(return_code) && "SUCCESS".equals(result_code)) {
            map.put("return_code", "SUCCESS");
            map.put("out_trade_no", (String) resultMap.get("out_trade_no"));
            return map;
        } else {
            map.put("return_code", "FAIL");
            return map;
        }
    }

    /**
     * 建立请求，以表单HTML形式构造（默认）
     */
    public static String buildRequest(Map<String, String> sParaTemp, String success_url, String cancel_url, String fail_url) {
        // ---------------------
        StringBuffer sbHtml = new StringBuffer();
        sbHtml.append("<title>微信安全支付</title>");
        sbHtml.append("<script>");
        sbHtml.append("document.addEventListener('WeixinJSBridgeReady', function onBridgeReady() {");
        sbHtml.append("WeixinJSBridge.invoke('getBrandWCPayRequest',{");
        sbHtml.append("'appId' : '").append(sParaTemp.get("appid")).append("','timeStamp' : '")
                .append(sParaTemp.get("timeStamp")).append("', 'nonceStr' : '")
                .append(sParaTemp.get("nonceStr")).append("', 'package' : '")
                .append(sParaTemp.get("package")).append("','signType' : 'GdMD5', 'paySign' : '")
                .append(sParaTemp.get("sign")).append("'");
        sbHtml.append("},function(res){");
        sbHtml.append("WeixinJSBridge.log(res.err_msg);");
        sbHtml.append("if(res.err_msg == 'get_brand_wcpay_request:ok'){");
        sbHtml.append("alert('微信支付成功!'); window.location.href='" + success_url + "'");
        sbHtml.append("}else if(res.err_msg == 'get_brand_wcpay_request:cancel'){  ");
        sbHtml.append("alert('用户取消支付!');window.location.href='" + cancel_url + "'");
        sbHtml.append("}else{alert('支付失败!'+res.err_msg);window.location.href='" + fail_url + "'}");
        sbHtml.append("});");
        sbHtml.append("}, false);");
        sbHtml.append("</script>");
        return sbHtml.toString();
    }

    /**
     * 获取微信支付所需form
     *
     * @param finalmoney  支付金额
     * @param orderCode   微信支付商家交易号
     * @param productDesc 产品描述
     * @param openId      wechat openid
     * @param notify_url  回调链接
     * @param request     request
     * @param response    response
     * @return
     */
    public static String buildPayResponse(String finalmoney, String orderCode, String productDesc,
                                          String openId, String notify_url, String success_url,
                                          String cancel_url, String fail_url,
                                          HttpServletRequest request, HttpServletResponse response) {
        finalmoney = finalmoney.replace(".", "");
        String currTime = GdDateTimeUtils.getTimeString("yyyyMMddHHmmss");
        // 8位日期
        String strTime = currTime.substring(8, currTime.length());
        // 四位随机数
        String strRandom = GdNumberUtils.buildRandom(4) + "";
        // 10位序列号,可以自行调整。
        String strReq = strTime + strRandom;
        String mch_id = GdWechatConfig.partner;
        // 附加数据
        String attach = "others";
        // 总金额以分为单位，不带小数点
        int total_fee = parseInt(finalmoney);
        // 订单生成的机器 IP
        String spbill_create_ip = request.getRemoteAddr();
        // 这里notify_url是 支付完成后微信发给该链接信息，可以判断会员是否支付成功，改变订单状态等。
        String trade_type = "JSAPI";
        SortedMap<String, String> packageParams = new TreeMap<String, String>();
        packageParams.put("appid", GdWechatConfig.appid);
        packageParams.put("mch_id", mch_id);
        packageParams.put("nonce_str", strReq);
        packageParams.put("body", productDesc);
        packageParams.put("attach", attach);
        packageParams.put("out_trade_no", orderCode);
        packageParams.put("total_fee", String.valueOf(total_fee));
        packageParams.put("spbill_create_ip", spbill_create_ip);
        packageParams.put("notify_url", notify_url);
        packageParams.put("trade_type", trade_type);
        packageParams.put("openid", openId);
        GdRequestHandler reqHandler = new GdRequestHandler(request, response);
        reqHandler.init(GdWechatConfig.appid, GdWechatConfig.secret, GdWechatConfig.partnerkey);
        String sign = reqHandler.createSign(packageParams);
        String xml = "<xml>" +
                "<appid>" + GdWechatConfig.appid + "</appid>" +
                "<mch_id>" + mch_id + "</mch_id>" +
                "<nonce_str>" + strReq + "</nonce_str>" +
                "<sign>" + sign + "</sign>" +
                "<body><![CDATA[" + productDesc + "]]></body>" +
                "<attach>" + attach + "</attach>" +
                "<out_trade_no>" + orderCode + "</out_trade_no>" +
                //金额，这里写的1 分到时修改
                "<total_fee>" + total_fee + "</total_fee>" +
                //"<total_fee>"+finalmoney+"</total_fee>"+
                "<spbill_create_ip>" + spbill_create_ip + "</spbill_create_ip>" +
                "<notify_url>" + notify_url + "</notify_url>" +
                "<trade_type>" + trade_type + "</trade_type>" +
                "<openid>" + openId + "</openid>" +
                "</xml>";
        System.out.println(xml);
        String createOrderURL = "https://api.mch.weixin.qq.com/pay/unifiedorder";
        String prepay_id = "";
        try {
            prepay_id = GdGetWxOrderno.getPayNo(createOrderURL, xml);
            if (StringUtils.isEmpty(prepay_id)) {
                request.setAttribute("ErrorMsg", "统一支付接口获取预支付订单出错");
                return "统一支付接口获取预支付订单出错";
            }
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        SortedMap<String, String> finalpackage = new TreeMap<String, String>();
        String appid2 = GdWechatConfig.appid;
        String timestamp = GdSha1Util.getTimeStamp();
        String packages = "prepay_id=" + prepay_id;
        finalpackage.put("appId", appid2);
        finalpackage.put("timeStamp", timestamp);
        finalpackage.put("nonceStr", strReq);
        finalpackage.put("package", packages);
        finalpackage.put("signType", "GdMD5");
        String finalsign = reqHandler.createSign(finalpackage);
        Map<String, String> sParaTemp = new HashMap<String, String>();
        sParaTemp.put("appid", appid2);
        sParaTemp.put("timeStamp", timestamp);
        sParaTemp.put("nonceStr", strReq);
        sParaTemp.put("package", packages);
        sParaTemp.put("sign", finalsign);
        return buildRequest(sParaTemp, success_url, cancel_url, fail_url);
    }
}
