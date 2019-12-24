package az.gov.adra.service;

import az.gov.adra.dataTransferObjects.ReservationDTO;
import az.gov.adra.dataTransferObjects.ReservationDTOForAdd;
import az.gov.adra.dataTransferObjects.ReservationDTOForUpdate;
import az.gov.adra.entity.Reservation;
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
    public void addReservation(ReservationDTOForAdd dto) throws ReservationCredentialsException {
        reservationRepository.addReservation(dto);
    }

    @Override
    public void updateReservation(ReservationDTOForUpdate dto) throws ReservationCredentialsException {
        reservationRepository.updateReservation(dto);
    }

    @Override
    public void isReservationExistWithGivenId(long id) throws ReservationCredentialsException {
        reservationRepository.isReservationExistWithGivenId(id);
    }

    @Override
    public void isReservationExistWithGivenReservation(ReservationDTOForAdd dto) throws ReservationCredentialsException {
        reservationRepository.isReservationExistWithGivenReservation(dto);
    }

    @Override
    public void deleteReservation(Reservation reservation) throws ReservationCredentialsException {
        reservationRepository.deleteReservation(reservation);
    }

//    @Override
//    public void addReservation(Reservation reservation) throws ReservationCredentialsException {
//        reservationRepository.addReservation(reservation);
//    }
//
//    @Override
//    public Reservation findReservationById(long id) {
//        return reservationRepository.findReservationById(id);
//    }
//
//    @Override
//    public List<ReservationDTO> findReservationsByEmployeeId(int employeeId, int fetchNext) {
//        return reservationRepository.findReservationsByEmployeeId(employeeId, fetchNext);
//    }
//
//    @Override
//    public void updateReservation(Reservation reservation, boolean hasChanged) throws ReservationCredentialsException {
//        reservationRepository.updateReservation(reservation, hasChanged);
//    }
//
//    @Override
//    public void deleteReservationById(long reservationId, int employeeId) throws ReservationCredentialsException {
//        reservationRepository.deleteReservationById(reservationId, employeeId);
//    }
//
//    @Override
//    public List<ReservationDTO> findReservationsByStatusAndStatus2(int status, int status2, int fetchNext) {
//        return reservationRepository.findReservationsByStatusAndStatus2(status, status2, fetchNext);
//    }
//
//    @Override
//    public void updateReservationStatusAndStatus2(int status, int status2, int reservationId, int employeeId) throws ReservationCredentialsException {
//        reservationRepository.updateReservationStatusAndStatus2(status, status2, reservationId, employeeId);
//    }
//
//    @Override
//    public void updateReservationStatus() {
//        reservationRepository.updateReservationStatus();
//    }

}

