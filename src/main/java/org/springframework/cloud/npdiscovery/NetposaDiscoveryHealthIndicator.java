package org.springframework.cloud.npdiscovery;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.actuate.health.Health;
import org.springframework.cloud.client.discovery.health.DiscoveryHealthIndicator;

public class NetposaDiscoveryHealthIndicator implements DiscoveryHealthIndicator {

    private static final Log log = LogFactory.getLog(NetposaDiscoveryHealthIndicator.class);

    private NetposaServiceDiscovery npClient;

    public NetposaDiscoveryHealthIndicator(NetposaServiceDiscovery client) {
        this.npClient = client;
    }

    @Override
    public String getName() {
        return "npDiscovery";
    }

    @Override
    public Health health() {
        Health.Builder builder = Health.unknown();
        try {
            List<NetposaDiscoveryInstanceInfo> instances = new ArrayList<>();
            List<String> names = npClient.getInstanceNames();
            for (String name : names) {
                instances.addAll(npClient.getInstancesById(name));
            }
            builder.up().withDetail("services", instances);
        } catch (Exception e) {
            log.error("Error", e);
            builder.down(e);
        }

        return builder.build();
    }
}
