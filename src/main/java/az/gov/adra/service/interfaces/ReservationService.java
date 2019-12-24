package az.gov.adra.service.interfaces;

import az.gov.adra.dataTransferObjects.ReservationDTO;
import az.gov.adra.dataTransferObjects.ReservationDTOForAdd;
import az.gov.adra.dataTransferObjects.ReservationDTOForUpdate;
import az.gov.adra.entity.Reservation;
import az.gov.adra.entity.User;
import az.gov.adra.exception.ReservationCredentialsException;

import java.util.List;

public interface ReservationService {

    List<ReservationDTO> findAllReservationsByStatus(int status, int fetchNext);

    List<ReservationDTO> findMyReservations(String username, int status, int fetchNext);

    List<ReservationDTO> findReservationsWhichIJoined(String username, int status, int fetchNext);

    void addReservation(ReservationDTOForAdd dto) throws ReservationCredentialsException;

    void updateReservation(ReservationDTOForUpdate dto) throws ReservationCredentialsException;

    List<User> findUsersOfReservationById(long id);

    void isReservationExistWithGivenId(long id) throws ReservationCredentialsException;

    void isReservationExistWithGivenReservation(ReservationDTOForAdd dto) throws ReservationCredentialsException;

    void deleteReservation(Reservation reservation) throws ReservationCredentialsException;

//    Reservation findReservationById(long id);
//
//    List<ReservationDTO> findReservationsByEmployeeId(int employeeId, int fetchNext);
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
