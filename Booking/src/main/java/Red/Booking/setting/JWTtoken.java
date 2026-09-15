package Red.Booking.setting;


import io.jsonwebtoken.security.Keys;
import org.hibernate.annotations.Comment;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;

@Component
public class JWTtoken {
    private final String SECRET = " Hi there, This Hemachandran i build now microwservies with spring boot framework";
    private final long TokenEXPIRATION = 10*60*1000;
    private final Key secretkey = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));

    // public
}
