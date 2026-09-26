package Red.Booking.setting;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;


import java.util.List;

@Slf4j
@Configuration
@EnableWebSecurity
public class security {

    @Value("${name.admin}")
    private String admin;

    /*Password Hashing*/
    @Bean
    public PasswordEncoder passwordEncoder() throws  RuntimeException{
        return new BCryptPasswordEncoder();
    }


    /*WebSecurity  API Access User name and password.*/
    @Bean
    public UserDetailsService userDetailsService() throws  RuntimeException{
        final var user = User.builder()
                .username("admin")
                .password("{noop}9944")
                .roles(admin)
                .build();

        return new InMemoryUserDetailsManager(user);
    }


    /* corsConfigurationSource this method is FrontEnd Only Corract PORT Number {Like Firewall or Definder}*/
    @Bean
    public CorsConfigurationSource corsConfigurationSource () throws  RuntimeException{
        try{
            CorsConfiguration corsConfiguration = new CorsConfiguration();
            corsConfiguration.setAllowedOrigins(List.of("http://localhost:8080"));
            corsConfiguration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE"));
            corsConfiguration.setAllowedHeaders(List.of("*"));

            UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
            source.registerCorsConfiguration("/**", corsConfiguration);

            return source;
        } catch (RuntimeException e) {
            throw new RuntimeException(new Exception().getMessage());
        }
    }


    /*Admin  and User API Access Configuration */
    @Bean
    public SecurityFilterChain Https_Filter(HttpSecurity http, JWTFilter jwtFilter) throws Exception{
        try {
            http

                    .csrf(AbstractHttpConfigurer::disable)
                    .cors(cors ->cors.configurationSource(corsConfigurationSource()))
                    .authorizeHttpRequests((auth )->auth
                            .requestMatchers(
                                    "/api/user/register",
                                    "/api/user/login",
                                    "/api/user/verify-otp",
                                    "/api/user/forgot-password",
                                    "/api/user/reset-password",
                                    "/swagger-ui/**",
                                    "/v3/api-docs/**",
                                    "/swagger-ui.html",
                                    "/health"
                            ).permitAll()

                            .requestMatchers(
                                    "/api/user/getAll-userDetail",
                                    "/api/user/deleteAllData",
                                    "/api/admin/**"
                            ).hasRole(admin)
                            .anyRequest().authenticated()
                    )
                    .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
            log.info("Security filiter chain configured successfulley");
            return http.build();
        } catch (RuntimeException exception) {
            System.out.println("error:"+exception.getMessage());
            throw exception;
        }

    }

}
