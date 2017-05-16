package org.springframework.cloud.npdiscovery;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.PreDestroy;

import org.springframework.boot.context.embedded.EmbeddedServletContainerInitializedEvent;
import org.springframework.cloud.client.DefaultServiceInstance;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.Environment;

public class NetposaDiscoveryClient
		implements DiscoveryClient, ApplicationListener<EmbeddedServletContainerInitializedEvent> {

	private final NetposaDiscoveryProperties prop;

	public static final String DESCRIPTION = "Spring Cloud Netposa Discovery Client";

	private final NetposaServiceDiscovery npClient;
	
	private Environment env;

	private AtomicInteger port = new AtomicInteger(0);

	public NetposaDiscoveryClient(NetposaDiscoveryProperties prop, NetposaServiceDiscovery npClient, Environment env) {
		super();
		this.prop = prop;
		this.npClient = npClient;
		this.env = env;
	}

	@Override
	public String description() {
		return DESCRIPTION;
	}

	@Override
	public ServiceInstance getLocalServiceInstance() {
		return new ServiceInstance() {
			@Override
			public String getServiceId() {
				return env.getProperty("spring.application.name");
			}

			@Override
			public String getHost() {
				return NetposaDiscoveryClient.this.prop.getInstanceHost();
			}

			@Override
			public int getPort() {
				return port.get();
			}

			@Override
			public boolean isSecure() {
				return false;
			}

			@Override
			public URI getUri() {
				return DefaultServiceInstance.getUri(this);
			}

			@Override
			public Map<String, String> getMetadata() {
				return new HashMap<>();
			}
		};
	}

	@Override
	public List<ServiceInstance> getInstances(String serviceId) {
		List<NetposaDiscoveryInstanceInfo> infos = this.npClient.getInstancesById(serviceId);
		List<ServiceInstance> instances = new ArrayList<>();
		for (NetposaDiscoveryInstanceInfo info : infos) {
			instances.add(new NpServiceInstance(info));
		}
		return instances;
	}

	public static class NpServiceInstance implements ServiceInstance {
		private NetposaDiscoveryInstanceInfo instance;

		NpServiceInstance(NetposaDiscoveryInstanceInfo instance) {
			this.instance = instance;
		}

		public NetposaDiscoveryInstanceInfo getInstanceInfo() {
			return instance;
		}

		@Override
		public String getServiceId() {
			return this.instance.getName();
		}

		@Override
		public String getHost() {
			return this.instance.getIp();
		}

		@Override
		public int getPort() {
			return this.instance.getPort();
		}

		@Override
		public boolean isSecure() {
			return false;
		}

		@Override
		public URI getUri() {
			return DefaultServiceInstance.getUri(this);
		}

		@Override
		public Map<String, String> getMetadata() {
			return new HashMap<>();
		}
	}

	@Override
	public List<String> getServices() {
		return this.npClient.getInstanceNames();
	}

	@Override
	public void onApplicationEvent(EmbeddedServletContainerInitializedEvent event) {
		this.port.compareAndSet(0, event.getEmbeddedServletContainer().getPort());

		startRegisterThread(new RegisterTask(npClient));
	}

	class RegisterTask extends TimerTask {

		private NetposaServiceDiscovery npClient;

		public RegisterTask(NetposaServiceDiscovery npClient) {
			super();
			this.npClient = npClient;
		}

		public NetposaServiceDiscovery getNpClient() {
			return npClient;
		}

		@Override
		public void run() {
			npClient.heart(getLocalServiceInstance());
		}
	}

	private void startRegisterThread(RegisterTask task) {
		task.getNpClient().register(getLocalServiceInstance());
		new Timer().scheduleAtFixedRate(task, 5000, 5000);
	}
	
	@PreDestroy
	public void destroy() {
	    npClient.unregister(getLocalServiceInstance());
	}
}
