package az.gov.adra.exception.handler;

import az.gov.adra.entity.response.Exception;
import az.gov.adra.entity.response.GenericResponse;
import az.gov.adra.exception.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@ControllerAdvice
public class GlobalExceptionHandler {

    @Value("${spring.app-name}")
    private String appName;

    @ExceptionHandler(DataAccessException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public GenericResponse handleDataAccessException(DataAccessException e) {
        //Exception
        Exception exception = new Exception();
        exception.setCode("0x0010");
        exception.setMessage(e.getMessage());
        exception.setErrorStack("DataAccessException.");

        return GenericResponse.withException(HttpStatus.INTERNAL_SERVER_ERROR, "DataAccessException", exception);
    }

    @ExceptionHandler(NumberFormatException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public GenericResponse handleNumberFormatException(NumberFormatException e) {
        //Exception
        Exception exception = new Exception();
        exception.setCode("0x0020");
        exception.setMessage(e.getMessage());
        exception.setErrorStack("NumberFormatException.");

        return GenericResponse.withException(HttpStatus.INTERNAL_SERVER_ERROR, "NumberFormatException", exception);
    }

    @ExceptionHandler(ReservationCredentialsException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public GenericResponse handleReservationCredentialsException(ReservationCredentialsException e) {
        //Exception
        Exception exception = new Exception();
        exception.setCode("0x0100");
        exception.setMessage(e.getMessage());
        exception.setErrorStack("ReservationCredentialsException.");

        return GenericResponse.withException(HttpStatus.INTERNAL_SERVER_ERROR, "ReservationCredentialsException", exception);
    }

}
