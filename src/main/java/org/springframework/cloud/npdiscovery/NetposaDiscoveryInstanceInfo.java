package org.springframework.cloud.npdiscovery;

/**
  * @ClassName: InstanceInfo
  * @Description: 注册实例信息
  * @author 
  * @date 2017年3月9日 上午9:28:28
  *
  */
public class NetposaDiscoveryInstanceInfo {

    private String name;
    
    private String ip;
    
    private int port;
    
    private String description;
    
    private long lastRegisterTime;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getLastRegisterTime() {
        return lastRegisterTime;
    }

    public void setLastRegisterTime(long lastRegisterTime) {
        this.lastRegisterTime = lastRegisterTime;
    }
}
