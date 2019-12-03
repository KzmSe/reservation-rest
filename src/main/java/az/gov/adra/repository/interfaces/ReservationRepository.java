package az.gov.adra.repository.interfaces;

import az.gov.adra.dataTransferObjects.ReservationDTO;
import az.gov.adra.entity.Reservation;
import az.gov.adra.exception.ReservationCredentialsException;


import java.util.List;

public interface ReservationRepository {

    List<ReservationDTO> findAllReservationsByStatus(int status, int fetchNext);

    List<ReservationDTO> findMyReservations(String username, int status, int fetchNext);

    List<ReservationDTO> findReservationsWhichIJoined(String username, int status, int fetchNext);

//    Reservation findReservationById(long id);
//
//    List<ReservationDTO> findReservationsByEmployeeId(int employeeId, int fetchNext);
//
//    void addReservation(Reservation r) throws ReservationCredentialsException;
//
//    void updateReservation(Reservation reservation, boolean hasChanged) throws ReservationCredentialsException;
//
//    void deleteReservationById(long reservationId, int employeeId) throws ReservationCredentialsException;
//
//    List<ReservationDTO> findReservationsByStatusAndStatus2(int status, int status2, int fetchNext);
//
//    void updateReservationStatusAndStatus2(int status, int status2, int reservationId, int employeeId) throws ReservationCredentialsException;
//
//    void updateReservationStatus();

}
