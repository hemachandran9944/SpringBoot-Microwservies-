package Red.Booking.controller;


import Red.Booking.model.users;
import Red.Booking.service.UserService;
import Red.Booking.setting.JWTtoken;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.util.LinkedHashMap;
import java.util.Map;



@RestController
@RequestMapping("api/user")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;
    private final JWTtoken jwTtoken;

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> Userresgister(@Valid @RequestBody users req) throws Exception{
        Map<String, Object> res = new LinkedHashMap<>();

        try {
            users resgisterUser = userService.ResgisterUser(req);
            res.put("status", "success");
            res.put("message", "user register successfully. Please verify OTP sent to your email.");
            res.put("id", resgisterUser.getId());
            res.put("name", resgisterUser.getName());
            res.put("gmail", resgisterUser.getGmail());
            res.put("otp", resgisterUser.getOtp());
            res.put("role", resgisterUser.getRole());
            log.info("User registered successfulley. Please verify OTP sent to your email.");
            return new ResponseEntity<>(res, HttpStatus.CREATED);
        } catch (Exception e) {
            Map<String, Object> errorRes = Map.of(
                    "status","error",
                    "message", e.getMessage()
            );
            return  new ResponseEntity<>(null,HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }




    @PostMapping("/verify-otp")
    public ResponseEntity<Map<String, Object>> verifyOTP(@RequestBody users req)throws RuntimeException{
        Map<String, Object> res = new LinkedHashMap<>();
        try {
            userService.verifyOtp(req.getGmail(), req.getOtp());
            res.put("status","success");
            res.put("message","OTP verify successfulley. Now you login");

            return  new ResponseEntity<>(res, HttpStatus.OK);
        } catch (RuntimeException exception) {
            Map<String, Object> errorRes = Map.of(
                    "status","error",
                    "message", exception.getMessage()
            );
            return new ResponseEntity<>(errorRes, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }




    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> userLogin(@RequestBody Map<String, String> body) throws  Exception{
        Map<String, Object> res= new LinkedHashMap<>();
        try{
            String gmail = body.get("gmail");
            String password = body.get("password");
            String role = body.get("role");

            if (gmail == null || password == null || gmail.isEmpty() || password.isEmpty()){
                res.put("status", "error");
                res.put("message", "Email and password are required");
                return new ResponseEntity<>(res, HttpStatus.BAD_REQUEST);
            }

            String token = userService.userLogin(gmail, password);

            res.put("status", "success");
            res.put("message", "Login Successfulley!");
            res.put("token",token);

            return  new ResponseEntity<>(res, HttpStatus.OK);
        }catch (RuntimeException exception){
            res.put("status", "error");
            res.put("message", exception.getMessage());
            return  new ResponseEntity<>(res, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }




    @GetMapping("/getAll-userDetail")
    public ResponseEntity<Map<String, Object>> getAllUserData() throws RuntimeException{
        Map<String, Object> res = new LinkedHashMap<>();
        try{

            var allUserData = userService.getAllUserData();

            res.put("status", "success");
            res.put("message", " get all user data successfulley!");
            res.put("totalUsers", allUserData.size());
            res.put("data", allUserData);

            return new ResponseEntity<>(res, HttpStatus.OK);
        }catch (RuntimeException exception) {
            res.put("status", "error");
            res.put("message", exception.getMessage());
            return  new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
        }catch (Exception exception) {
            res.put("status", "error");
            res.put("message", exception.getMessage());
            return  new ResponseEntity<>(res, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @GetMapping("/getUserDataByid/{id}")
    public ResponseEntity<Map<String, Object>> getByIdwithUserdata(@PathVariable Long id)
            throws  RuntimeException{
        Map<String, Object> res = new LinkedHashMap<>();
        try{
           final var getByIdUserData = userService.getUserDataById(id);

           if (getByIdUserData == null){
               res.put("status", "error");
               res.put("message", "user not found!");
               return  new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
           }

           res.put("status", "suucess");
           res.put("message", "getByid data successfulley!");
           res.put("data", getByIdUserData);
           return  new ResponseEntity<>(res, HttpStatus.OK);
        } catch (RuntimeException exception) {
            res.put("status", "error");
            res.put("message", exception.getMessage());
            return new ResponseEntity<>(res, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/updateUserDataById/{id}")
    public ResponseEntity<Map<String, Object>> updateUserDataWithById(@PathVariable Long id, @RequestBody users user) throws  RuntimeException{
        Map<String, Object> res = new LinkedHashMap<>();
        try {

            Map<String, Object> UpdateUserDateById = userService.updateById(id, user);

            if (UpdateUserDateById == null){
                res.put("status", "error");
                res.put("message", "user not foun");
                return  new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
            }

            res.put("status", "success");
            res.put("message", "Update user data successfully!");
            res.put("data", UpdateUserDateById);
            return  new ResponseEntity<>(res, HttpStatus.OK);
        } catch (RuntimeException exception) {
            res.put("status", "error");
            res.put("message", exception.getMessage());
            return new ResponseEntity<>(res, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/deleteSingleUser/{id}")
    public ResponseEntity<Map<String, Object>> deleteUserWithById(@PathVariable Long id) throws  RuntimeException{
        Map<String, Object> res = new LinkedHashMap<>();
        try{
             userService.deleteById(id);

             res.put("status", "success");
             res.put("message", "delete sigle user successfulley!");
             return  new ResponseEntity<>(res, HttpStatus.OK);
        } catch (RuntimeException exception) {
            res.put("status", "error");
            res.put("message", exception.getMessage());
            return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
        } catch (Throwable exception) {
            res.put("status", "error");
            res.put("message", exception.getMessage());
            return new ResponseEntity<>(res, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @DeleteMapping("/deleteAllData")
    public ResponseEntity<Map<String, Object>> deleteAllData() throws  RuntimeException{
        Map<String, Object> res = new LinkedHashMap<>();
        try{
            long deleteCount = userService.deleteAllData();

            if (deleteCount == 0){
                res.put("status", "error");
                res.put("message", "no record found");
                return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
            }

            res.put("status", "success");
            res.put("message", "delete all user data successfully!");
            res.put("data", deleteCount);
            return new ResponseEntity<>(res, HttpStatus.OK);
        } catch (RuntimeException exception) {
            res.put("status", "error");
            res.put("message", exception.getMessage());
            return new ResponseEntity<>(res, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
