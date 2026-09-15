package Red.Booking.setting;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class security {

    /*Password Hashing*/
    @Bean
    public PasswordEncoder passwordEncoder() throws  Exception{
        return new BCryptPasswordEncoder();
    }


    /*WebSecurity  API Access User name and password.*/

    @Bean
    public UserDetailsService userDetailsService() throws  Exception{
        final var user = User.builder()
                .username("admin")
                .password("{noop}9944")
                .roles("adminHema")
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
    public SecurityFilterChain Https_Filter(HttpSecurity http, )

}
