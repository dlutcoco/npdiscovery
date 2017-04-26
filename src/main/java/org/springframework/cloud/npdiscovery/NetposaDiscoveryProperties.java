package org.springframework.cloud.npdiscovery;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.commons.util.InetUtils;
import org.springframework.cloud.npdiscovery.ribbon.LoadBalancerType;

@ConfigurationProperties("spring.cloud.netposa.discovery")
public class NetposaDiscoveryProperties {

	private String connectString = "http://localhost:9800";

	private InetUtils.HostInfo hostInfo;

	private boolean enabled = true;

	private String uriSpec = "{scheme}://{address}:{port}";

	private String instanceHost;

	private Integer instancePort;

	private boolean register = true;

	private Map<String, String> metadata = new HashMap<>();

	private Map<String, LoadBalancerType> dependencies = new LinkedHashMap<>();

	@Value("${spring.cloud.netposa.discovery.dependency.ribbon.loadbalancer.defaulthealthendpoint:/health}")
	private String defaultHealthEndpoint;

	@SuppressWarnings("unused")
	private NetposaDiscoveryProperties() {
	}

	public NetposaDiscoveryProperties(InetUtils inetUtils) {
		this.hostInfo = inetUtils.findFirstNonLoopbackHostInfo();
		this.instanceHost = this.hostInfo.getIpAddress();
	}

	public String getConnectString() {
		return connectString;
	}

	public void setConnectString(String connectString) {
		this.connectString = connectString;
	}

	public boolean isEnabled() {
		return this.enabled;
	}

	public String getUriSpec() {
		return this.uriSpec;
	}

	public String getInstanceHost() {
		return this.instanceHost;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public void setUriSpec(String uriSpec) {
		this.uriSpec = uriSpec;
	}

	public void setInstanceHost(String instanceHost) {
		this.instanceHost = instanceHost;
		this.hostInfo.override = true;
	}

	public Map<String, String> getMetadata() {
		return this.metadata;
	}

	public void setMetadata(Map<String, String> metadata) {
		this.metadata = metadata;
	}

	public boolean isRegister() {
		return this.register;
	}

	public void setRegister(boolean register) {
		this.register = register;
	}

	public Integer getInstancePort() {
		return this.instancePort;
	}

	public void setInstancePort(Integer instancePort) {
		this.instancePort = instancePort;
	}

	public Map<String, LoadBalancerType> getDependencies() {
		return dependencies;
	}

	public void setDependencies(Map<String, LoadBalancerType> dependencies) {
		this.dependencies = dependencies;
	}

	public String getDefaultHealthEndpoint() {
		return defaultHealthEndpoint;
	}

	public void setDefaultHealthEndpoint(String defaultHealthEndpoint) {
		this.defaultHealthEndpoint = defaultHealthEndpoint;
	}
}
