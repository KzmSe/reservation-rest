package az.gov.adra.repository.interfaces;

import az.gov.adra.dataTransferObjects.ReservationDTO;
import az.gov.adra.entity.Reservation;
import az.gov.adra.entity.User;
import az.gov.adra.exception.ReservationCredentialsException;


import java.util.List;

public interface ReservationRepository {

    List<ReservationDTO> findAllReservationsByStatus(int status, int fetchNext);

    List<ReservationDTO> findMyReservations(String username, int status, int fetchNext);

    List<ReservationDTO> findReservationsWhichIJoined(String username, int status, int fetchNext);

    void addReservation(ReservationDTO dto) throws ReservationCredentialsException;

    void updateReservation(ReservationDTO dto) throws ReservationCredentialsException;

    List<User> findUsersOfReservationById(long id);

    void isReservationExistWithGivenId(long id) throws ReservationCredentialsException;

    void isReservationExistWithGivenDateAndTime(ReservationDTO dto) throws ReservationCredentialsException;

    void deleteReservation(Reservation reservation) throws ReservationCredentialsException;

    ReservationDTO findReservationById(long id);

    void updateReservationStatus();

}
