package OnlineBusBooking.Payment.payment_service.provider;



public record GatewayResult(boolean success, String txnId, String message) {

    public static GatewayResult ok(String txnId) {
        return new GatewayResult(true, txnId, "OK");
    }

    public static GatewayResult failed(String message) {
        return new GatewayResult(false, null, message);
    }
}
