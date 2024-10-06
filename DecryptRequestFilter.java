import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class DecryptRequestFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return exchange.getRequest().getBody().collectList().flatMap(dataBufferList -> {
            StringBuilder requestBody = new StringBuilder();
            dataBufferList.forEach(dataBuffer -> {
                byte[] bytes = new byte[dataBuffer.readableByteCount()];
                dataBuffer.read(bytes);
                requestBody.append(new String(bytes));
            });

            try {
                String decryptedBody = EncryptionUtil.decrypt(requestBody.toString());
                // Set the decrypted body in the request
                exchange.getAttributes().put("DECRYPTED_BODY", decryptedBody);
            } catch (Exception e) {
                e.printStackTrace();
            }

            return chain.filter(exchange);
        });
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE; // Ensures it's applied early
    }
}
