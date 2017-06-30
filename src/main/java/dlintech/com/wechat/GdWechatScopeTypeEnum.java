package dlintech.com.wechat;

/**
 * 微信获取用户信息类型
 */
public enum GdWechatScopeTypeEnum {

    SNSAPI_BASE("snsapi_base", "基本信息"),
    SNSAPI_USERINFO("snsapi_userinfo", "详细信息");
    private String typeCode;

    private String typeDesc;

    private GdWechatScopeTypeEnum(String typeCode, String typeDesc) {
        this.typeCode = typeCode;
        this.typeDesc = typeDesc;
    }

    public String getTypeCode() {
        return typeCode;
    }

    public void setTypeCode(String typeCode) {
        this.typeCode = typeCode;
    }

    public String getTypeDesc() {
        return typeDesc;
    }

    public void setTypeDesc(String typeDesc) {
        this.typeDesc = typeDesc;
    }
}
