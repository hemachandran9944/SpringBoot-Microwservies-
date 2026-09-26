package Red.Booking.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "users")
public class users {
    @Id
    @GeneratedValue
    Long id;


    @Size(min = 2, max = 100, message = "Name must be between 2  and  100 characers")
    @NotBlank(message = "name fileds requried")
    @Column(name = "name", nullable = false)
    String name;


    @Email(message = "invalid email formate")
    @NotBlank(message = "gmail fileds requried")
    @Column(name = "gmail", nullable = false)
    String gmail;


    @NotBlank
    @Column(name = "password", nullable = false)
    @NotBlank(message = "password fileds requried")
    @Size(min = 8, message = "Password must be at least 6 characters")
    String password;


    @Column(name = "otp", nullable = true)
    @Size(min = 6, message = "OTP must be 6 digits")
    private String otp;

    @Column(name = "OTPExpiration", nullable = true)
    private LocalDateTime otpExpiration;


    @Column(name = "isVerified", nullable = false)
    private Boolean isVerified = false;

    @Column(name = "role", nullable = true)
    private String role;


}
