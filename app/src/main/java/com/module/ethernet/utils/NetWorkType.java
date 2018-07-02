package com.module.ethernet.utils;

/**
 * @author Crystal lee
 * @package com.module.ethernet.utils
 * @fileName NetWorkType
 * @date on 2018/6/28 0028
 * @describe TODO
 */

public class NetWorkType {

    private String ip ;
    private EnumNetWorkType type ;

    public NetWorkType(String ip, EnumNetWorkType type) {
        this.ip = ip;
        this.type = type;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public EnumNetWorkType getType() {
        return type;
    }

    public void setType(EnumNetWorkType type) {
        this.type = type;
    }
}
