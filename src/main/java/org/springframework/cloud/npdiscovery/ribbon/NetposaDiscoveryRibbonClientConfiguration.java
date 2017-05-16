package org.springframework.cloud.npdiscovery.ribbon;

import static com.netflix.client.config.CommonClientConfigKey.DeploymentContextBasedVipAddresses;
import static com.netflix.client.config.CommonClientConfigKey.EnableZoneAffinity;

import javax.annotation.PostConstruct;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.npdiscovery.NetposaDiscoveryProperties;
import org.springframework.cloud.npdiscovery.NetposaServiceDiscovery;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.netflix.client.config.IClientConfig;
import com.netflix.config.ConfigurationManager;
import com.netflix.config.DynamicPropertyFactory;
import com.netflix.config.DynamicStringProperty;
import com.netflix.loadbalancer.ILoadBalancer;
import com.netflix.loadbalancer.IPing;
import com.netflix.loadbalancer.PingUrl;
import com.netflix.loadbalancer.ServerList;

@Configuration
public class NetposaDiscoveryRibbonClientConfiguration {
    
    private static final Log log = LogFactory.getLog(RibbonNetposaAutoConfiguration.class);

    protected static final String VALUE_NOT_SET = "__not__set__";
    protected static final String DEFAULT_NAMESPACE = "ribbon";

    @Autowired
    private NetposaServiceDiscovery serviceDiscovery;
    
    @Value("${ribbon.client.name}")
    private String serviceId = "client";

    public NetposaDiscoveryRibbonClientConfiguration() {
    }

//    @Bean
//    @ConditionalOnMissingBean
//    @ConditionalOnProperty(value = "spring.cloud.netposa.discovery.dependency.ribbon.loadbalancer", matchIfMissing = true)
    public ILoadBalancer dependenciesBasedLoadBalancer(ServerList<NetposaServer> serverList, IClientConfig config,
            IPing iPing) {
        System.out.println("init bean ILoadBalancer");
        return new DependenciesBasedLoadBalancer(serverList, config, iPing);
    }

    @Bean
    @ConditionalOnMissingBean
    public IPing healthCheckingRule(NetposaDiscoveryProperties netposaDiscoveryProperties) {
        System.out.println("init bean IPing");
        return new PingUrl(false, netposaDiscoveryProperties.getDefaultHealthEndpoint());
    }

    @Bean
    @ConditionalOnMissingBean
    public ServerList<?> ribbonServerList(IClientConfig config) {
        System.out.println("init bean ServerList");
        NetposaServerList serverList = new NetposaServerList(this.serviceDiscovery);
        serverList.initWithNiwsConfig(config);
        log.debug(String.format("Server list for Ribbon's non-dependency based load balancing is [%s]", serverList));
        return serverList;
    }

    @PostConstruct
    public void preprocess() {
        setProp(this.serviceId, DeploymentContextBasedVipAddresses.key(), this.serviceId);
        setProp(this.serviceId, EnableZoneAffinity.key(), "true");
    }

    protected void setProp(String serviceId, String suffix, String value) {
        String key = getKey(serviceId, suffix);
        DynamicStringProperty property = getProperty(key);
        if (property.get().equals(VALUE_NOT_SET)) {
            ConfigurationManager.getConfigInstance().setProperty(key, value);
        }
    }

    protected DynamicStringProperty getProperty(String key) {
        return DynamicPropertyFactory.getInstance().getStringProperty(key, VALUE_NOT_SET);
    }

    protected String getKey(String serviceId, String suffix) {
        return serviceId + "." + DEFAULT_NAMESPACE + "." + suffix;
    }
}
