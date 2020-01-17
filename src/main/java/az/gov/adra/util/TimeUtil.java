package az.gov.adra.util;

import az.gov.adra.constant.MessageConstants;
import az.gov.adra.exception.ReservationCredentialsException;

import java.time.LocalTime;

public interface TimeUtil {

    static void checkTime(LocalTime startTime, LocalTime endTime) throws ReservationCredentialsException {
        if (endTime.isBefore(startTime)) {
            throw new ReservationCredentialsException(MessageConstants.ERROR_MESSAGE_STARTTIME_MUST_BE_BEFORE_THAN_ENDTIME);
        }
    }

}
