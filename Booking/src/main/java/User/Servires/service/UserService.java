package User.Servires.service;

import User.Servires.model.Users;
import User.Servires.repository.UserRepository;
import User.Servires.setting.EmailMessage;
import User.Servires.setting.JWTtoken;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
public class UserService {

    @Value("${google.password}")
    private  String GoogelOauthPassword;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailMessage emailMessage;

    @Autowired
    private JWTtoken jwTtoken;

    @Transactional
    public Users ResgisterUser(Users req){
        try {
            if (req.getGmail() == null || req.getGmail().isEmpty()) {
                throw new RuntimeException("Email cannot be empty");
            }

            if (req.getName() == null || req.getName().isEmpty()) {
                throw new RuntimeException("Name cannot be empty");
            }

            if (req.getPassword() == null || req.getPassword().isEmpty()) {
                throw new RuntimeException("Password cannot be empty");
            }

            if (req.getPassword().length() < 6) {
                throw new RuntimeException("must be at least 6 characters");
            }

            Optional<Users> existingUser = userRepository.findByGmail(req.getGmail());
            if (existingUser.isPresent()) {
                log.warn("User already exists with email:{}", req.getGmail());
                throw new RuntimeException(req.getGmail());
            }

            Users newUser = Users.builder()
                    .name(req.getName())
                    .gmail(req.getGmail())
                    .password(passwordEncoder.encode(req.getPassword()))
                    .role(req.getRole() != null ? req.getRole() : "USER")
                    .isVerified(false)
                    .build();

            Users savedUser = userRepository.save(newUser);
            log.warn("User registered successfully with email:{}", req.getGmail());

            String otp = generateOTP();
            sendVerificationOtp(savedUser, otp);

            return savedUser;
        } catch (RuntimeException exception) {
            log.warn("User registration validation failed: {}", exception.getMessage());
            throw exception;
        } catch (Exception exception) {
            log.warn("Unexpected error during user registration:{}", exception.getMessage(), exception);
            //throw new RuntimeException("Failed to register user: " + exception.getMessage());
            throw new RuntimeException("User already exists with email: " + req.getGmail());

        }
    }

    @Transactional
    public void sendVerificationOtp(Users user, String otp) {
        try {
            user.setOtp(otp);
            user.setOtpExpiration(LocalDateTime.now().plusMinutes(10));
            userRepository.save(user);
            emailMessage.registerOTP(user.getName(), user.getGmail(), otp);
            log.info("OTP sent successfully to email:{}", user.getGmail());
        } catch (RuntimeException e) {
            throw new RuntimeException(e.getMessage());
        }
    }


    private String generateOTP() throws Exception {
        try {
            Random random = new Random();
            int otp = 100000 + random.nextInt(900000);
            return String.valueOf(otp);
        } catch (RuntimeException e) {
            throw new RuntimeException(e.getMessage());
        }
    }




    @Transactional
    public void verifyOtp(String gmail, String otp) {
        try {
            Users exitUser = userRepository.findByGmail(gmail)
                    .orElseThrow(() -> new RuntimeException("user not found"));

            if (exitUser.getIsVerified()) {
                throw new RuntimeException("already verified");
            }

            if (exitUser.getOtp() == null || !exitUser.getOtp().equals(otp)) {
                throw new RuntimeException("Wrong Otp");
            }

            if (LocalDateTime.now().isAfter(exitUser.getOtpExpiration())) {
                throw new RuntimeException("Otp Expired");
            }

            exitUser.setIsVerified(true);
            exitUser.setOtp(null);
            exitUser.setOtpExpiration(null);
            userRepository.save(exitUser);

            log.info("OTP verified successfully for email:{}", gmail);
        } catch (RuntimeException e) {
            log.warn("OTP verification failed: {}", e.getMessage());
            throw e;
        }
    }

    public String userLogin(String gmail, String password) throws Exception {
        try {
            Users exitUser = userRepository.findByGmail(gmail)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            if (!exitUser.getIsVerified()) {
                throw new RuntimeException("Please verify your email using OTP before login");
            }

            if (exitUser.getPassword() == null) {
                throw new RuntimeException("Password not found for this user");
            }

            if (!passwordEncoder.matches(password, exitUser.getPassword())) {
                throw new RuntimeException("Invalid password");
            }

            String token = jwTtoken.generateToken(exitUser.getGmail(), exitUser.getRole());
            log.info("User logged in successfully: {}", gmail);

            return token;
        } catch (RuntimeException e) {
            log.warn("Login failed: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during login: {}", e.getMessage());
            throw new RuntimeException("Login failed: " + e.getMessage());
        }
    }


    public Map<String, Object> getUserDataById(Long id) throws RuntimeException {
        try {
            Users user = userRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Map<String, Object> userMap = new LinkedHashMap<>();
            userMap.put("id", user.getId());
            userMap.put("name", user.getName());
            userMap.put("gmail", user.getGmail());
            userMap.put("role", user.getRole());

            return userMap;
        } catch (RuntimeException exception) {
            throw new RuntimeException(exception.getMessage());
        }
    }

    public List<Users> getAllUserData() throws RuntimeException {
        try {
            List<Users> Getalldata = userRepository.findAll();

            if (Getalldata.isEmpty()) {
                throw new RuntimeException("No record not found!");
            }
            return Getalldata;
        } catch (RuntimeException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public Map<String, Object> updateById(Long id, Users user) throws RuntimeException {
        try {
            Users findByUserId = userRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("user not found"));

            findByUserId.setName(user.getName());
            findByUserId.setGmail(user.getGmail());

            Users updateUser = userRepository.save(findByUserId);

            Map<String, Object> userMap = new LinkedHashMap<>();
            userMap.put("id", updateUser.getId());
            userMap.put("name", updateUser.getName());
            userMap.put("gmail", updateUser.getGmail());

            return userMap;
        } catch (RuntimeException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public void deleteById(Long id) throws RuntimeException {
        try {
            Users deleteSigleUser = userRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("user not found"));
            userRepository.deleteById(id);
        } catch (RuntimeException exception) {
            throw new RuntimeException(exception.getMessage());
        }
    }

    public long deleteAllData() {
        try {
            final long DeleteCount = userRepository.count();
            userRepository.deleteAll();
            return DeleteCount;
        } catch (RuntimeException exception) {
            throw new RuntimeException(exception.getMessage());
        }
    }


    @Transactional
    public Map<String, Object> forgetPassword(String gmail) throws  Exception{
        Map<String, Object> res = new LinkedHashMap<>();
        try{
            Users user = userRepository.findByGmail(gmail)
                    .orElseThrow(()-> new RuntimeException("User not found!"));

            String otp = generateOTP();
            user.setOtp(otp);
            user.setOtpExpiration(LocalDateTime.now().plusMinutes(10));
            userRepository.save(user);

            emailMessage.forgetPasswordOtp(user.getName(), gmail, otp);
            res.put("status", "message");
            res.put("message", "password reset OTP sent  successfully to your email!");
            res.put("otp", otp);
            return  res;
        } catch (RuntimeException e) {
            throw new RuntimeException(e.getMessage());
        }

    }


    @Transactional
    public Map<String, Object> resetPassword (String gmail, String otp, String newPassword, String confirmPassword)throws Exception{
        Map<String, Object> res = new LinkedHashMap<>();
        try{
            Users user = userRepository.findByGmail(gmail)
                    .orElseThrow(()->new RuntimeException("User not found!"));

            if (user.getOtp() == null  || !user.getOtp().equals(otp)){
                throw new RuntimeException("Worng OTP");
            }

            if (user.getOtpExpiration() != null && LocalDateTime.now().isAfter(user.getOtpExpiration())) {
                throw new RuntimeException("OTP has expired!");
            }

            if (!newPassword.equals(confirmPassword)){
                throw new RuntimeException("Password do not match!");
            }

            if (newPassword == null || newPassword.isEmpty() || confirmPassword == null || confirmPassword.isEmpty()) {
                throw new RuntimeException("Password required!");
            }

            user.setPassword(passwordEncoder.encode(newPassword));
            user.setOtp(null);
            user.setOtpExpiration(null);
            userRepository.save(user);

            res.put("status", "success");
            res.put("message", "Password reset successfully!");

            return res;
        } catch (RuntimeException exception) {
            throw new RuntimeException(exception);
        }
    }


    @Transactional
    public Map<String, Object> processGooegleLogin (String gmail, String name, String image, String googleId)throws RuntimeException   {
        Map<String, Object> res = new LinkedHashMap<>();
        try {
            Users user = userRepository.findByGmail(gmail).orElse(null);

            if (user == null){
                user = new Users();
                user.setGmail(gmail);
                user.setName(name);
                user.setImage(image);
                user.setGoogleId(googleId);
                user.setPassword(passwordEncoder.encode(GoogelOauthPassword));
                user.setRole("USER");
                user.setIsVerified(true);
                userRepository.save(user);
                log.info("New Google user registered successfully: {}", gmail);
            }

            String token = jwTtoken.generateToken(user.getGmail(), user.getRole());

            res.put("status", "success");
            res.put("message", "Google Login Successfully!");
            res.put("token", token);
            res.put("gmail", gmail);
            res.put("name", name);
            res.put("image", image);
            res.put("googleId", googleId);

            return res;
        } catch (Exception e) {
            log.error("Google login error: {}", e.getMessage());
            throw new RuntimeException("Google Login Failed: " + e.getMessage());
        }
    }

}