package Red.Booking.setting;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
public class    EmailMessage {

    @Autowired
    private JavaMailSender  javaMailSender;

    public  void registerOTP(String name,String gmail, String otp) throws RuntimeException{
        try{
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(gmail);
            message.setSubject("Registration - Red Bus Booking");

            String registerMessage = "Hello " + name + ",\n\n" +
                    "Welcome to Red Bus Booking! We are thrilled to have you on board.\n\n" +
                    "To complete your registration and secure your account, please use the One-Time Password (OTP) provided below:\n\n" +
                    "Your Verification OTP: " + otp + "\n\n" +
                    "This OTP is valid for 5 minutes. Please do not share this confidential code with anyone.\n\n" +
                    "If you did not request this registration, please ignore this email.\n\n" +
                    "Best Regards,\n" +
                    "Team Red Bus Booking";

            message.setText(registerMessage);
            javaMailSender.send(message);
        } catch (RuntimeException exception) {
            throw new RuntimeException("Failed to send email: " + exception.getMessage());

        }
    }


    public  void forgetPasswordOtp(String name, String gmail, String otp)throws  RuntimeException{
        try{
            SimpleMailMessage forgetEmail = new SimpleMailMessage();
            forgetEmail.setTo(gmail);
            forgetEmail.setSubject("ForgetPassowordOtp - Red Bus Booking");

            String forgotPasswordMessage = "Hello " + name + ",\n\n" +
                    "We received a request to reset the password for your Red Bus Booking account.\n\n" +
                    "To proceed with your password reset, please use the One-Time Password (OTP) provided below:\n\n" +
                    "Your Password Reset OTP: " + otp + "\n\n" +
                    "This OTP is valid for 10 minutes. Please do not share this confidential code with anyone.\n\n" +
                    "If you did not request a password reset, please ignore this email or contact support if you have concerns.\n\n" +
                    "Best Regards,\n" +
                    "Team Red Bus Booking";
            forgetEmail.setText(forgotPasswordMessage);
            javaMailSender.send(forgetEmail);
        } catch (RuntimeException exception) {
            throw new RuntimeException(exception.getMessage());
        }
    }


}

