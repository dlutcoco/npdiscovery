package org.springframework.cloud.npdiscovery;

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

	@Bean
	public NetposaDiscoveryProperties netposaDiscoveryProperties(InetUtils inetUtils) {
		return new NetposaDiscoveryProperties(inetUtils);
	}

	@Bean
	public NetposaServiceDiscovery NpClient(NetposaDiscoveryProperties prop) {
		return new NetposaServiceDiscovery(prop);
	}

	@Bean
	public NetposaDiscoveryClient discoveryClient(NetposaDiscoveryProperties prop, NetposaServiceDiscovery npClient, Environment env) {
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
