package az.gov.adra.job;

import az.gov.adra.service.interfaces.ReservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ReservationJOB {

    @Autowired
    private ReservationService reservationService;

    /**
     * This job updates reservations which DATE = TODAY and END_TIME <= CURRENT_TIME after every 5 min
     */
    @Scheduled(fixedRate = 300000)
    public void changeReservationStatusJOB() {
        try {
            reservationService.updateReservationStatus();

        } catch (Exception e) {
            System.out.println("Exception: " + e.getMessage());
        }
    }

}