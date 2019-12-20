# Gateway with custom Discovery Client

This is a spring-cloud-gateway using a custom discovery client.
It serves as a complete and minimal example demonstrating that the discovery client's information is not refreshed.

## How it works

The `custom.discovery.gateway.CustomDiscoveryClient` returns one of three clients each time it is called. The client is determined pseudo-randomly using a fixed random seed.
The Gateway calls the discovery client on startup (and on the first request?), but refrains from calling it on any further request, leading all requests to go to a single target instance. 

The `DiscoveryNotRefreshedDemonstrationTest` features a single test-case demonstrating this effect. It constructs three backend service instances using wiremock, covering the range of potential service instances that could be returned by the discovery client if discovery information where updated.
The test fails as only a single backend service (`wireMockServer2`) is called.


## Expected Behavior

The `custom.discovery.gateway.CustomDiscoveryClient` should be re-queried for successive requests, checking if discovery information has changed. 

## Notes
No cache is included on the classpath. Therefore, I do not expect caching to be the reason. As expected, setting a cache `ttl` and adding `Thread.sleep` in between calls to the test did not help.

The example uses a `ReactiveDiscoveryClient`, but the effect occurs with a regular `DiscoveryClient` as well.

The code uses Lombok and the test is written using Groovy Spock.
