package kt.aivle.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.stereotype.Component;

@Component
public class TestHeaderFilter extends AbstractGatewayFilterFactory<TestHeaderFilter.Config> {

    public TestHeaderFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String path = exchange.getRequest().getPath().value();
            
            // shorts API에 대해서만 테스트 헤더 추가
            if (path.startsWith("/api/shorts")) {
                exchange = exchange.mutate()
                    .request(r -> r.header("X-USER-ID", "5")
                                   .header("X-STORE-ID", "2"))
                    .build();
            }
            
            return chain.filter(exchange);
        };
    }

    public static class Config {
        // Configuration properties if needed
    }
}