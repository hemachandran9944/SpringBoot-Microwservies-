package Red.Booking.service;

import Red.Booking.model.users;
import Red.Booking.repository.UserRepository;
import Red.Booking.setting.EmailMessage;
import Red.Booking.setting.JWTtoken;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;


import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;



import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;


@Slf4j
@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailMessage emailMessage;

    @Autowired
    private JWTtoken jwTtoken;

    @Transactional
    public users ResgisterUser(users req ){
        try{
            if (req.getGmail() == null || req.getGmail().isEmpty()){
                throw new RuntimeException("Email cannot be empty");
            }

            if (req.getName() == null || req.getName().isEmpty()){
                throw new RuntimeException("Name cannot be empty");
            }

            if(req.getPassword() == null || req.getPassword().isEmpty()){
                throw new RuntimeException("Password cannot be empty ");
            }

            if (req.getPassword().length()<6){
                throw new RuntimeException("must be at least 6 charactres");
            }

            Optional<users> existingUser = userRepository.findByGmail(req.getGmail());
            if (existingUser.isPresent()){
                log.warn("User already exists with email:{}", req.getGmail());
                throw new RuntimeException(req.getGmail());
            }

            users newUser = users.builder()
                    .name(req.getName())
                    .gmail(req.getGmail())
                    .password(passwordEncoder.encode(req.getPassword()))
                    .role(req.getRole() != null ? req.getRole() : "USER")
                    .isVerified(false)
                    .build();

            users savedUser = userRepository.save(newUser);
            log.warn("User registered successfully with email:{}", req.getGmail());

            String otp = generateOTP();
            sendVerificationOtp(savedUser, otp);

            return savedUser;
        } catch (RuntimeException exception) {
            log.warn("User registration validation failed: {}", exception.getMessage());
            throw exception;
        } catch (Exception exception) {
            log.warn("Unexpected error during user registration:{}", exception.getMessage(), exception);
            throw new RuntimeException("Failed to register user: " + exception.getMessage());
        }
    }



    private  String generateOTP() throws Exception{
        try {
            Random random = new Random();
            int otp = 100000 + random.nextInt(900000);
            return String.valueOf(otp);
        } catch (RuntimeException e) {
            throw new RuntimeException(e.getMessage());
        }
    }


    @Transactional
    public void sendVerificationOtp(users user, String otp){
        try {
            user.setOtp(otp);
            user.setOtpExpiration(LocalDateTime.now().plusMinutes(10));
            userRepository.save(user);
            emailMessage.registerOTP(user.getName(), user.getGmail(), otp);
            log.info("OTP sent successfulley to email:{}", user.getGmail());
        } catch (RuntimeException e) {
            throw new RuntimeException(e.getMessage());
        }
    }



    @Transactional
    public void verifyOtp(String gmail, String otp){
        try {

            users exitUser = userRepository.findByGmail(gmail)
                    .orElseThrow(()-> new RuntimeException("user not foun"));

            if (exitUser.getIsVerified()){
                throw  new RuntimeException("already verified");
            }

            if(exitUser.getOtp() == null || !exitUser.getOtp().equals(otp)){
                throw new RuntimeException("Wrong Otp");
            }

            if (LocalDateTime.now().isAfter(exitUser.getOtpExpiration())){
                throw new RuntimeException("Otp Expired");
            }

            exitUser.setIsVerified(true);
            exitUser.setOtp(null);
            exitUser.setOtpExpiration(null);
            userRepository.save(exitUser);

            log.info("OTP verified successfulley for email:{}", gmail);
        } catch (RuntimeException e) {
            log.warn("OTP verification failed: {}", e.getMessage());
            throw e;
        }
    }


    public String userLogin(String gmail, String password) throws Exception {
        try {
            users exitUser = userRepository.findByGmail(gmail)
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

    public Map<String, Object> getUserDataById(Long id) throws  RuntimeException{
        try{
            users user = userRepository.findById(id)
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



    public List<users> getAllUserData ()throws  RuntimeException{
        try{
            List<users> Getalldata = userRepository.findAll();

            if (Getalldata.isEmpty()){
                throw new RuntimeException("No record not found!");
            }
            return  Getalldata;
        } catch (RuntimeException e) {
            throw new RuntimeException(e.getMessage());
        }
    }


    public users updateById(Long id , users user) throws RuntimeException{
        try {
            users findByUserId = userRepository.findById(id)
                    .orElseThrow(()->new RuntimeException("user not found"));

            findByUserId.setName(user.getName());
            findByUserId.setGmail(user.getGmail());
            return userRepository.save(findByUserId);
        } catch (RuntimeException e) {
            throw new RuntimeException(e.getMessage());
        }
    }


    public void deleteById(Long id) throws RuntimeException{
        try {
           users deleteSigleUser =  userRepository.findById(id)
                   .orElseThrow(()->new RuntimeException("user not found"));
           userRepository.deleteById(id);
        } catch (RuntimeException exception) {
            throw new RuntimeException(exception.getMessage());
        }
    }



}




