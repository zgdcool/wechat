package dlintech.com.wechat;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import dlintech.com.tools.http.GDHttpClient;
import org.apache.http.entity.StringEntity;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

@SuppressWarnings("deprecation")
public class GdGetWxOrderno {

    /**
     * 解析 统一支付接口调用 获取结果,返回最重要的预支付ID参数，prepay_id，
     *
     * @param url
     * @param xmlParam
     * @return
     */
    @SuppressWarnings({"rawtypes"})
    public static String getPayNo(String url, String xmlParam) {
        System.out.println("xml是:" + xmlParam);
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("Connection", "keep-alive");
        headers.put("Accept", "*/*");
        headers.put("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        headers.put("Host", "api.mch.weixin.qq.com");
        headers.put("X-Requested-With", "XMLHttpRequest");
        headers.put("Cache-Control", "max-age=0");
        headers.put("User-Agent", "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.0) ");
        String jsonStr = GDHttpClient.post(url, null, headers, new StringEntity(xmlParam, "UTF-8"));
        System.out.println("json是:" + jsonStr);
        String prepay_id = "";
        if (jsonStr.contains("FAIL")) {
            return prepay_id;
        }
        try {
            Map map = doXMLParse(jsonStr);
            prepay_id = (String) map.get("prepay_id");
            return prepay_id;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 解析xml,返回第一级元素键值对。如果第一级元素有子节点，则此节点的值是子节点的xml数据。
     *
     * @param strxml
     * @return
     * @throws IOException
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static Map doXMLParse(String strxml) throws Exception {
        if (null == strxml || "".equals(strxml)) {
            return null;
        }
        Map m = new HashMap();
        InputStream in = String2Inputstream(strxml);
        SAXBuilder builder = new SAXBuilder();
        Document doc = builder.build(in);
        Element root = doc.getRootElement();
        List list = root.getChildren();
        Iterator it = list.iterator();
        while (it.hasNext()) {
            Element e = (Element) it.next();
            String k = e.getName();
            String v;
            List children = e.getChildren();
            if (children.isEmpty()) {
                v = e.getTextNormalize();
            } else {
                v = getChildrenText(children);
            }
            m.put(k, v);
        }
        // 关闭流
        in.close();

        return m;
    }

    /**
     * 获取子结点的xml
     *
     * @param children
     * @return String
     */
    @SuppressWarnings({"rawtypes"})
    private static String getChildrenText(List children) {
        StringBuffer sb = new StringBuffer();
        if (!children.isEmpty()) {
            Iterator it = children.iterator();
            while (it.hasNext()) {
                Element e = (Element) it.next();
                String name = e.getName();
                String value = e.getTextNormalize();
                List list = e.getChildren();
                sb.append("<").append(name).append(">");
                if (!list.isEmpty()) {
                    sb.append(getChildrenText(list));
                }
                sb.append(value);
                sb.append("</").append(name).append(">");
            }
        }

        return sb.toString();
    }

    public static InputStream String2Inputstream(String str) {
        return new ByteArrayInputStream(str.getBytes());
    }

}