package Red.Booking.setting;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class JWTtoken {



    @Value("${jwt.secret}")
    private  String secret;

    @Value("${jwt.expiration}")
    private  long expiration;

    private Key secretkey;

    @PostConstruct
    public void init(){
        try{
            if (secret == null || secret.length()<32){
                throw new IllegalArgumentException("JWT secret must be at leaset 32 characters");
            }
            this.secretkey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
            log.info("JWT component initialized successfully");
        } catch (RuntimeException exception) {
            log.info("Error initialized JWT component:{}", exception.getMessage());
            throw new RuntimeException("Failed to initialize JWT component", exception);
        }
    }




    public String generateToken(String gmail, String role) throws JwtException{

        if (gmail == null || gmail.trim().isEmpty()){
            throw new IllegalArgumentException("Email cannot be null or empty");
        }


        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role);

        try{

            String token =Jwts.builder()
                    .setClaims(claims)
                    .setSubject(gmail)
                    .setIssuedAt(new Date(System.currentTimeMillis()))
                    .setExpiration(new Date(System.currentTimeMillis()+expiration))
                    .signWith(secretkey, SignatureAlgorithm.HS256)
                    .compact();
            log.info("JWT token generated successfully for user: {}", gmail);
            return token;
        } catch (JwtException exception) {
            log.warn("Error generating JWT token:{}", exception);
            throw new RuntimeException(exception.getMessage());
        } catch (RuntimeException e) {
            log.error("Unexpected error generating JWT token: {}", e.getMessage(), e);
            throw new RuntimeException(e.getMessage());
        }
    }


    public String extractEmail(String token) throws JwtException{
        try{
            String gmail = Jwts.parser()
                    .setSigningKey(secretkey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject();
            log.info("Email extracted successfulley");
            return gmail;
        } catch (JwtException exception) {
            throw new RuntimeException(exception.getMessage());
        }
    }



    public String extractRole(String token) throws JwtException{
        try{
            String role = Jwts.parser()
                    .setSigningKey(secretkey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .get("role", String.class);


            log.info("Role extracted successfulley form token");
            return role;
        } catch (JwtException exception) {
            throw new RuntimeException(exception.getMessage());
        }
    }


    public boolean ValidateJwtToken(String token) throws RuntimeException{
        try{
            if (token == null || token.isEmpty()){
                log.info("Token validation failed: Token is null or empty");
                return false;
            }
            Jwts.parser()
                    .setSigningKey(secretkey)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException |    IllegalArgumentException exception){
            log.warn("JWT validation failed: {}", exception.getMessage());
            return false;
        }
    }
}
