package br.edu.atitus.gateway_service.filters;

import java.util.List;
import br.edu.atitus.gateway_service.configs.ApiGatewayConfig;
import io.jsonwebtoken.Claims;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import br.edu.atitus.gateway_service.GatewayServiceApplication;
import br.edu.atitus.gateway_service.components.JwtUtil;
import reactor.core.publisher.Mono;

@Component
public class AuthFilter implements GlobalFilter, Ordered {

    private final GatewayServiceApplication gatewayServiceApplication;

    private final ApiGatewayConfig apiGatewayConfig;

	private static final List<String> PROTECTED_ROUTES = List.of("/ws/");

    AuthFilter(ApiGatewayConfig apiGatewayConfig, GatewayServiceApplication gatewayServiceApplication) {
        this.apiGatewayConfig = apiGatewayConfig;
        this.gatewayServiceApplication = gatewayServiceApplication;
    }
	
	@Override
	public int getOrder() {
		return -1;
	}

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

		var request = exchange.getRequest();
		String path = request.getURI().getPath();
		
		if (!PROTECTED_ROUTES.stream().anyMatch(path::startsWith))
			return chain.filter(exchange);
		
		//Verificar a existencia de token e validar o token
		String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
		if (authHeader != null && authHeader.startsWith("Bearer ")) {
			String jwt = authHeader.substring(7);
			Claims payload = JwtUtil.validateToken(jwt);
			if (payload != null) {
				ServerHttpRequest modifiedRequest = request.mutate()
						.header("X-User-Id", String.valueOf(payload.get("id", long.class)))
						.header("X-User-Type", String.valueOf(payload.get("type", Integer.class)))						
						.header("X-User-Email", payload.get("email", String.class))
						.build();
				return chain.filter(exchange.mutate().request(modifiedRequest).build());
			} 
			
		}
		
		exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
		return exchange.getResponse().setComplete();
	}

}
