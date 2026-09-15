package Red.Booking.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
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

    @NotBlank
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
    String password;


    @Column(name = "otp", nullable = true)
    private String otp;

    @Column(name = "OTPExpiration", nullable = true)
    private LocalDateTime otpExpiration;


    @Column(name = "isVerified", nullable = false)
    private Boolean isVerified = false;

}
