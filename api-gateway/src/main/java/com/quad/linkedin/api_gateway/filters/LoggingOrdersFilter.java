package com.quad.linkedin.api_gateway.filters;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.stereotype.Component;


@Slf4j
@Component
public class LoggingOrdersFilter extends AbstractGatewayFilterFactory<LoggingOrdersFilter.Config> {

    public LoggingOrdersFilter(){
        super();
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            log.info("Order Filter Pre: {}",exchange.getRequest().getURI());
            return chain.filter(exchange);
        };
    }

    /// This class can be used to take arguments from the application.yml
    ///  (just after defining filter in api-gateway's .yml file)
    public static class Config{}
}

// Route Specific or Gateway filter are extended
// from AbstractGatewayFilterFactory
//
//@Override
//public GatewayFilter apply(Config config) {
//    return new GatewayFilter() {
//        @Override
//        public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
//            return null;
//        }
//    };
//}

/// To use : filters:
///             - name: LoggingOrdersFilter