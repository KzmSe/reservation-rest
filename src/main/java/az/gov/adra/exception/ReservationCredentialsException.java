package az.gov.adra.exception;

public class ReservationCredentialsException extends Exception {

    public ReservationCredentialsException(String message) {
        super(message);
    }

    public ReservationCredentialsException(String message, Throwable cause) {
        super(message, cause);
    }
}
