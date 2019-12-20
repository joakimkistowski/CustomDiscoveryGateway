package custom.discovery.gateway;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.ReactiveDiscoveryClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.io.Serializable;
import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;


@Component
@AllArgsConstructor
@Slf4j
@Value
public class CustomDiscoveryClient implements ReactiveDiscoveryClient {

    private static final String DISCOVERY_CLIENT_DESCRIPTION
            = "Discovery client randomly returning a port of 8081, 8082, 8083 each time it is called.";

    private Random r = new Random(5);

    @Override
    public String description() {
        return DISCOVERY_CLIENT_DESCRIPTION;
    }

    @Override
    public Flux<ServiceInstance> getInstances(String serviceId) {
        int port = 8081 + r.nextInt(3);
        List<ServiceInstance> instances = Collections.singletonList(
                new SerializableServiceInstance(
                        serviceId, "localhost", port, URI.create("http://localhost:" + port), "http"
                )
        );
        LOGGER.info("Returning instances: {}", instances);
        return Flux.fromIterable(instances);
    }

    @Override
    public Flux<String> getServices() {
        return Flux.just("service");
    }

    @Override
    public int getOrder() {
        return -1;
    }


    @AllArgsConstructor
    @ToString
    @EqualsAndHashCode
    public static class SerializableServiceInstance implements ServiceInstance, Serializable {

        private static final long serialVersionUID = 3393159585071988676L;

        private String serviceId;
        private String host;
        private int port;
        private URI uri;
        private String scheme;

        @Override
        public String getServiceId() {
            return serviceId;
        }

        @Override
        public String getHost() {
            return host;
        }

        @Override
        public int getPort() {
            return port;
        }

        @Override
        public boolean isSecure() {
            return false;
        }

        @Override
        public URI getUri() {
            return uri;
        }

        @Override
        public Map<String, String> getMetadata() {
            return new HashMap<>();
        }

        @Override
        public String getScheme() {
            return scheme;
        }
    }
}
