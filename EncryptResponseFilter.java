import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class EncryptResponseFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            exchange.getResponse().beforeCommit(() -> {
                try {
                    String responseBody = "original response body";  // Replace this with actual body extraction logic
                    String encryptedBody = EncryptionUtil.encrypt(responseBody);
                    // Set the encrypted body to the response
                    exchange.getResponse().getHeaders().add("Content-Type", "application/json");
                    exchange.getResponse().getHeaders().add("Encrypted", "true"); // Custom header to indicate encryption
                    exchange.getResponse().writeWith(Mono.just(exchange.getResponse().bufferFactory().wrap(encryptedBody.getBytes())));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return Mono.empty();
            });
        }));
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE; // Ensures it's applied last
    }
}
