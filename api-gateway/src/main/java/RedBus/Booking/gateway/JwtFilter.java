package RedBus.Booking.gateway;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.List;


@Component
public class JwtFilter implements GlobalFilter, Ordered {
    @Value("${jwt.secret}")
    private  String secret;

    @Value("${name.admin}")
    private String admin;


    private static final List<String> PUBLIC_PATHS = List.of(
            "/api/user/register",
            "/api/user/login",
            "/api/user/verify-otp",
            "/api/user/forgot-password",
            "/api/user/reset-password",
            "/api/user/google-success"
    );

    private  static final List<String> PUBLIC_PREFIXES  = List.of("/oauth2/", "/login/");

    private boolean isPublic(String path) throws RuntimeException{
        return PUBLIC_PATHS.contains(path) || PUBLIC_PREFIXES.stream().anyMatch(path :: startsWith);
    }



    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) throws RuntimeException {
        String path = exchange.getRequest().getURI().getPath();

        if (isPublic(path)){
            return chain.filter(exchange);
        }

        String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")){
            return reject(exchange, HttpStatus.UNAUTHORIZED);
        }

        try{
            SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(authHeader.substring(7))
                    .getPayload();
            String role = claims.get("role", String.class);


            HttpMethod method = exchange.getRequest().getMethod();
            if (path.startsWith("/api/bus") &&
                    !HttpMethod.GET.equals(method) &&
                    !"ADMIN".equals(role)) {
                return reject(exchange, HttpStatus.FORBIDDEN);
            }

            ServerHttpRequest GatWayRequest = exchange.getRequest().mutate()
                    .headers(h -> {
                        h.remove("X-User-Email");
                        h.remove("X-User-Role");
                    })
                    .header("X-User-Email", claims.getSubject())
                    .header("X-User-Role", role)
                    .build();

            return chain.filter(exchange.mutate().request(GatWayRequest).build());
        } catch (Exception error) {
            System.out.println("JWT error: " + error.getMessage());
            return reject(exchange, HttpStatus.UNAUTHORIZED);
        }
    }

    private Mono<Void> reject(ServerWebExchange exchange, HttpStatus status) {
        exchange.getResponse().setStatusCode(status);
        return exchange.getResponse().setComplete();
    }

    public int getOrder() {
        return -1;
    }


}
