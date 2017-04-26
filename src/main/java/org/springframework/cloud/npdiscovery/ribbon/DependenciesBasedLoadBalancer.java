package org.springframework.cloud.npdiscovery.ribbon;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.DynamicServerListLoadBalancer;
import com.netflix.loadbalancer.IPing;
import com.netflix.loadbalancer.IRule;
import com.netflix.loadbalancer.RandomRule;
import com.netflix.loadbalancer.RoundRobinRule;
import com.netflix.loadbalancer.Server;
import com.netflix.loadbalancer.ServerList;

public class DependenciesBasedLoadBalancer extends DynamicServerListLoadBalancer<NetposaServer> {
    
    private static final Log log = LogFactory.getLog(DependenciesBasedLoadBalancer.class);

    private final Map<String, IRule> ruleCache = new ConcurrentHashMap<>();

    public DependenciesBasedLoadBalancer(ServerList<NetposaServer> serverList, IClientConfig config, IPing iPing) {
        super(config);
        setServersList(serverList.getInitialListOfServers());
        setPing(iPing);
        setServerListImpl(serverList);
    }

    @Override
    public Server chooseServer(Object key) {
        String keyAsString = (String) key;
        cacheEntryIfMissing(keyAsString);
        log.debug(String.format("Will try to retrieve dependency for key [%s]. Current cache contents [%s]", keyAsString, this.ruleCache));
        updateListOfServers();
        return this.ruleCache.get(keyAsString).choose(key);
    }

    private void cacheEntryIfMissing(String keyAsString) {
        if (!this.ruleCache.containsKey(keyAsString)) {
            log.debug(String.format("Cache doesn't contain entry for [%s]", keyAsString));
            this.ruleCache.put(keyAsString, chooseRuleForLoadBalancerType(LoadBalancerType.ROUND_ROBIN));
        }
    }

    private IRule chooseRuleForLoadBalancerType(LoadBalancerType type) {
        switch (type) {
            case ROUND_ROBIN:
                return getRoundRobinRule();
            case RANDOM:
                return getRandomRule();
            case STICKY:
                return getStickyRule();
            default:
                throw new IllegalArgumentException("Unknown load balancer type " + type);
        }
    }

    private RoundRobinRule getRoundRobinRule() {
        return new RoundRobinRule(this);
    }

    private IRule getRandomRule() {
        RandomRule randomRule = new RandomRule();
        randomRule.setLoadBalancer(this);
        return randomRule;
    }

    private IRule getStickyRule() {
        StickyRule stickyRule = new StickyRule(getRoundRobinRule());
        stickyRule.setLoadBalancer(this);
        return stickyRule;
    }
}
