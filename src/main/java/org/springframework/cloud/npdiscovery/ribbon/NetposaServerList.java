package org.springframework.cloud.npdiscovery.ribbon;

import static org.springframework.util.ReflectionUtils.rethrowRuntimeException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.cloud.npdiscovery.NetposaDiscoveryInstanceInfo;
import org.springframework.cloud.npdiscovery.NetposaServiceDiscovery;

import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.AbstractServerList;

public class NetposaServerList extends AbstractServerList<NetposaServer> {

    private String serviceId;
    private final NetposaServiceDiscovery serviceDiscovery;

    public NetposaServerList(NetposaServiceDiscovery serviceDiscovery) {
        this.serviceDiscovery = serviceDiscovery;
    }

    @Override
    public void initWithNiwsConfig(IClientConfig clientConfig) {
        this.serviceId = clientConfig.getClientName();
    }

    @Override
    public List<NetposaServer> getInitialListOfServers() {
        return getServers();
    }

    @Override
    public List<NetposaServer> getUpdatedListOfServers() {
        return getServers();
    }

    @SuppressWarnings("unchecked")
    private List<NetposaServer> getServers() {
        try {
            if (this.serviceDiscovery == null) {
                return Collections.EMPTY_LIST;
            }
            List<NetposaDiscoveryInstanceInfo> instances = this.serviceDiscovery
                    .getInstancesById(this.serviceId);
            if (instances == null || instances.isEmpty()) {
                return Collections.EMPTY_LIST;
            }
            List<NetposaServer> servers = new ArrayList<>();
            for (NetposaDiscoveryInstanceInfo instance : instances) {
                servers.add(new NetposaServer(instance));
            }
            return servers;
        }
        catch (Exception e) {
            rethrowRuntimeException(e);
        }
        return Collections.EMPTY_LIST;
    }
}
