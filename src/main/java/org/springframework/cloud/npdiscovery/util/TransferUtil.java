package org.springframework.cloud.npdiscovery.util;

import java.util.Date;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.npdiscovery.NetposaDiscoveryInstanceInfo;

public class TransferUtil {

    public static NetposaDiscoveryInstanceInfo castNpInstance(ServiceInstance instance) {
        NetposaDiscoveryInstanceInfo info = new NetposaDiscoveryInstanceInfo();
        info.setIp(instance.getHost());
        info.setPort(instance.getPort());
        info.setName(instance.getServiceId());
        info.setLastRegisterTime(new Date().getTime());
        
        return info;
    }
}
