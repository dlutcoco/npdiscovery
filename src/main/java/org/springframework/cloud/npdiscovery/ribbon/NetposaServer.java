package org.springframework.cloud.npdiscovery.ribbon;

import org.springframework.cloud.npdiscovery.NetposaDiscoveryInstanceInfo;

import com.netflix.loadbalancer.Server;

public class NetposaServer extends Server {

    private final MetaInfo metaInfo;

    public NetposaServer(final NetposaDiscoveryInstanceInfo instance) {
        super(instance.getIp(), instance.getPort());
        this.metaInfo = new MetaInfo() {
            @Override
            public String getAppName() {
                return instance.getName();
            }

            @Override
            public String getServerGroup() {
                return null;
            }

            @Override
            public String getServiceIdForDiscovery() {
                return null;
            }

            @Override
            public String getInstanceId() {
                return instance.getName();
            }
        };
    }

    @Override
    public MetaInfo getMetaInfo() {
        return this.metaInfo;
    }
}
