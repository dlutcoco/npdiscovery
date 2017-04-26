package org.springframework.cloud.npdiscovery.ribbon;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.AbstractLoadBalancerRule;
import com.netflix.loadbalancer.IRule;
import com.netflix.loadbalancer.Server;

public class StickyRule extends AbstractLoadBalancerRule {
    private static final Log log = LogFactory.getLog(StickyRule.class);
    private final IRule masterStrategy;
    private final AtomicReference<Server> ourInstance = new AtomicReference<>(null);
    private final AtomicInteger instanceNumber = new AtomicInteger(-1);

    public StickyRule(IRule masterStrategy) {
        this.masterStrategy = masterStrategy;
    }

    @Override
    public void initWithNiwsConfig(IClientConfig iClientConfig) {

    }

    @Override
    public Server choose(Object key) {
        final List<Server> instances = getLoadBalancer().getServerList(true);
        log.debug(String.format("Instances taken from load balancer [%s]", instances));
        Server localOurInstance = this.ourInstance.get();
        log.debug(String.format("Current saved instance [%s]", localOurInstance));
        if (!instances.contains(localOurInstance)) {
            this.ourInstance.compareAndSet(localOurInstance, null);
        }
        if (this.ourInstance.get() == null) {
            Server instance = this.masterStrategy.choose(key);
            if (this.ourInstance.compareAndSet(null, instance)) {
                this.instanceNumber.incrementAndGet();
            }
        }
        return this.ourInstance.get();
    }

    /**
     * Each time a new instance is picked, an internal counter is incremented.
     * This way you can track when/if the instance changes. The instance can
     * change when the selected instance is not in the current list of instances
     * returned by the instance provider
     *
     * @return instance number
     */
    public int getInstanceNumber() {
        return this.instanceNumber.get();
    }
}
