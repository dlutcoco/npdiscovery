package org.springframework.cloud.npdiscovery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.Endpoint;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.commons.util.InetUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
@ConditionalOnProperty(value = "spring.cloud.netposa.discovery.enabled", matchIfMissing = true)
public class NetposaDiscoveryAutoConfiguration {

    private static final Logger LOG = LoggerFactory.getLogger(NetposaDiscoveryAutoConfiguration.class);
    
	@Bean
	public NetposaDiscoveryProperties netposaDiscoveryProperties(InetUtils inetUtils) {
	    LOG.info("init bean NetposaDiscoveryProperties");
		return new NetposaDiscoveryProperties(inetUtils);
	}

	@Bean
	public NetposaServiceDiscovery NpClient(NetposaDiscoveryProperties prop) {
	    LOG.info("init bean NetposaServiceDiscovery");
		return new NetposaServiceDiscovery(prop);
	}

	@Bean
	public NetposaDiscoveryClient discoveryClient(NetposaDiscoveryProperties prop, NetposaServiceDiscovery npClient, Environment env) {
	    LOG.info("init bean NetposaDiscoveryClient");
		return new NetposaDiscoveryClient(prop, npClient, env);
	}
	
	@Configuration
    @ConditionalOnClass(Endpoint.class)
    protected static class NetposaDiscoveryHealthConfig {
        @Autowired
        private NetposaServiceDiscovery npClient;

        @Bean
        @ConditionalOnMissingBean
        public NetposaDiscoveryHealthIndicator netposaDiscoveryHealthIndicator() {
            return new NetposaDiscoveryHealthIndicator(this.npClient);
        }
    }
}
