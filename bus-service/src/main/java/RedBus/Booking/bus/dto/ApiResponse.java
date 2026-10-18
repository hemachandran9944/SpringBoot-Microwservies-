package RedBus.Booking.bus.dto;

public record ApiResponse<T> (String status, String message, T data){
    public static <T> ApiResponse<T> success(String message, T data){
        return new ApiResponse<T>("success", message, data);
    }

    public static <T> ApiResponse<T> error(String message){
        return new ApiResponse<>("error", message, null);
    }

}
