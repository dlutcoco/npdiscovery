package org.springframework.cloud.npdiscovery;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.npdiscovery.util.RegisterHttpClient;
import org.springframework.cloud.npdiscovery.util.TransferUtil;

import com.alibaba.fastjson.JSON;

public class NetposaServiceDiscovery {

    private static final Logger log = LoggerFactory.getLogger(NetposaServiceDiscovery.class);

    private final NetposaDiscoveryProperties prop;

    private final String URL_REGISTER = "/cms/register";
    private final String URL_UNREGISTER = "/cms/unregister";
    private final String URL_HEART = "/cms/heart";
    private final String URL_QUERY_SERVICES = "/cms/services";

    public NetposaServiceDiscovery(NetposaDiscoveryProperties prop) {
        this.prop = prop;
    }

    public void register(ServiceInstance instance) {
        try {
            String res = RegisterHttpClient.getInstance().httpPostJson(prop.getConnectString() + URL_REGISTER,
                    JSON.toJSONString(TransferUtil.castNpInstance(instance)));
            if (JSON.parseObject(res).getInteger("code") != 200) {
                throw new RuntimeException("register error, res=" + res);
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("register error...");
        }
    }

    public void heart(ServiceInstance instance) {
        try {
            String res = RegisterHttpClient.getInstance().httpPostJson(prop.getConnectString() + URL_HEART,
                    JSON.toJSONString(TransferUtil.castNpInstance(instance)));
            if (JSON.parseObject(res).getInteger("code") != 200) {
                throw new RuntimeException("heart error, res=" + res);
            }
        } catch (Exception e) {
            log.error("heart error...");
        }
    }

    public void unregister(ServiceInstance instance) {
        try {
            String res = RegisterHttpClient.getInstance().httpPostJson(prop.getConnectString() + URL_UNREGISTER,
                    JSON.toJSONString(TransferUtil.castNpInstance(instance)));
            if (JSON.parseObject(res).getInteger("code") != 200) {
                throw new RuntimeException("heart error, res=" + res);
            }
        } catch (Exception e) {
            log.error("heart error...");
        }
    }

    public List<NetposaDiscoveryInstanceInfo> getInstancesById(String serviceId) {
        if (serviceId == null) {
            throw new RuntimeException("param serviceId must not be null");
        }

        List<NetposaDiscoveryInstanceInfo> serviceList = new ArrayList<>();
        try {
            String res = RegisterHttpClient.getInstance().httpGet(prop.getConnectString() + URL_QUERY_SERVICES + "/" + serviceId, null);
            if (JSON.parseObject(res).getInteger("code") != 200) {
                throw new RuntimeException("getInstancesById error, res=" + res);
            } else {
                serviceList = JSON.parseObject(res).getJSONObject("data").getJSONArray("services").toJavaList(NetposaDiscoveryInstanceInfo.class);
            }
        } catch (Exception e) {
            log.error("getInstancesById error...");
        }

        return serviceList;
    }

    public List<String> getInstanceNames() {
        List<String> serviceNames = new ArrayList<>();
        try {
            String res = RegisterHttpClient.getInstance().httpGet(prop.getConnectString() + URL_QUERY_SERVICES, null);
            if (JSON.parseObject(res).getInteger("code") != 200) {
                throw new RuntimeException("getInstancesById error, res=" + res);
            } else {
                serviceNames = JSON.parseObject(res).getJSONObject("data").getJSONArray("serviceNames").toJavaList(String.class);
            }
        } catch (Exception e) {
            log.error("getInstancesById error...");
        }

        return serviceNames;
    }

}
