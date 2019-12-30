package az.gov.adra.service;

import az.gov.adra.dataTransferObjects.ReservationDTO;
import az.gov.adra.entity.Reservation;
import az.gov.adra.entity.User;
import az.gov.adra.exception.ReservationCredentialsException;
import az.gov.adra.repository.interfaces.ReservationRepository;
import az.gov.adra.service.interfaces.ReservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReservationServiceImpl implements ReservationService {

    private ReservationRepository reservationRepository;

    @Autowired
    public ReservationServiceImpl(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    @Override
    public List<ReservationDTO> findAllReservationsByStatus(int status, int fetchNext) {
        return reservationRepository.findAllReservationsByStatus(status, fetchNext);
    }

    @Override
    public List<ReservationDTO> findMyReservations(String username, int status, int fetchNext) {
        return reservationRepository.findMyReservations(username, status, fetchNext);
    }

    @Override
    public List<ReservationDTO> findReservationsWhichIJoined(String username, int status, int fetchNext) {
        return reservationRepository.findReservationsWhichIJoined(username, status, fetchNext);
    }

    @Override
    public void addReservation(ReservationDTO dto) throws ReservationCredentialsException {
        reservationRepository.addReservation(dto);
    }

    @Override
    public void updateReservation(ReservationDTO dto) throws ReservationCredentialsException {
        reservationRepository.updateReservation(dto);
    }

    @Override
    public List<User> findUsersOfReservationById(long id) {
        return reservationRepository.findUsersOfReservationById(id);
    }

    @Override
    public void isReservationExistWithGivenId(long id) throws ReservationCredentialsException {
        reservationRepository.isReservationExistWithGivenId(id);
    }

    @Override
    public void isReservationExistWithGivenDateAndTime(ReservationDTO dto) throws ReservationCredentialsException {
        reservationRepository.isReservationExistWithGivenDateAndTime(dto);
    }

    @Override
    public void deleteReservation(Reservation reservation) throws ReservationCredentialsException {
        reservationRepository.deleteReservation(reservation);
    }

    @Override
    public ReservationDTO findReservationById(long id) {
        return reservationRepository.findReservationById(id);
    }

    @Override
    public void updateReservationStatus() {
        reservationRepository.updateReservationStatus();
    }

}

