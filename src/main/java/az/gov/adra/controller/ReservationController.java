package az.gov.adra.controller;

import az.gov.adra.constant.MessageConstants;
import az.gov.adra.dataTransferObjects.ReservationDTO;
import az.gov.adra.entity.Reservation;
import az.gov.adra.entity.User;
import az.gov.adra.entity.response.GenericResponse;
import az.gov.adra.exception.ReservationCredentialsException;
import az.gov.adra.service.interfaces.ReservationService;
import az.gov.adra.util.EmailSenderUtil;
import az.gov.adra.util.TimeUtil;
import az.gov.adra.util.ValidationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@RestController
public class ReservationController {

    @Autowired
    private ReservationService reservationService;
    @Autowired
    private EmailSenderUtil emailSenderUtil;


    @GetMapping("/reservations")
    @PreAuthorize("hasRole('ROLE_USER')")
    public GenericResponse findAllReservations(@RequestParam(name = "status", required = false) Integer status,
                                               @RequestParam(name = "fetchNext", required = false) Integer fetchNext) throws ReservationCredentialsException {
        if (ValidationUtil.isNull(status) || ValidationUtil.isNull(fetchNext)) {
            throw new ReservationCredentialsException(MessageConstants.ERROR_MESSAGE_ONE_OR_MORE_FIELDS_ARE_EMPTY);
        }

        List<ReservationDTO> reservations = reservationService.findAllReservationsByStatus(status, fetchNext);
        return GenericResponse.withSuccess(HttpStatus.OK, "list of reservations", reservations);
    }

    @GetMapping("/reservations/me")
    @PreAuthorize("hasRole('ROLE_USER')")
    public GenericResponse findMyReservations(@RequestParam(name = "status", required = false) Integer status,
                                              @RequestParam(name = "fetchNext", required = false) Integer fetchNext,
                                              Principal principal) throws ReservationCredentialsException {
        if (ValidationUtil.isNull(status) || ValidationUtil.isNull(fetchNext)) {
            throw new ReservationCredentialsException(MessageConstants.ERROR_MESSAGE_ONE_OR_MORE_FIELDS_ARE_EMPTY);
        }

        List<ReservationDTO> reservations = reservationService.findMyReservations(principal.getName(), status, fetchNext);
        return GenericResponse.withSuccess(HttpStatus.OK, "list of my reservations", reservations);
    }

    @GetMapping("/reservations/joined")
    @PreAuthorize("hasRole('ROLE_USER')")
    public GenericResponse findReservationsWhichIJoined(@RequestParam(name = "status", required = false) Integer status,
                                                      @RequestParam(name = "fetchNext", required = false) Integer fetchNext,
                                                      Principal principal) throws ReservationCredentialsException {
        if (ValidationUtil.isNull(status) || ValidationUtil.isNull(fetchNext)) {
            throw new ReservationCredentialsException(MessageConstants.ERROR_MESSAGE_ONE_OR_MORE_FIELDS_ARE_EMPTY);
        }

        List<ReservationDTO> reservations = reservationService.findReservationsWhichIJoined(principal.getName(), status, fetchNext);
        return GenericResponse.withSuccess(HttpStatus.OK, "list of reservations which I joined", reservations);
    }

    @PostMapping("/reservations")
    @PreAuthorize("hasRole('ROLE_USER')")
    @ResponseStatus(HttpStatus.CREATED)
    public void addReservation(@RequestBody ReservationDTO dto,
                               Principal principal) throws ReservationCredentialsException {
        if (ValidationUtil.isNullOrEmpty(dto.getTopic()) || ValidationUtil.isNull(dto.getDate(), dto.getStartTime(), dto.getEndTime(), dto.getRoomId())) {
            throw new ReservationCredentialsException(MessageConstants.ERROR_MESSAGE_ONE_OR_MORE_FIELDS_ARE_EMPTY);
        }
        TimeUtil.checkTime(dto.getStartTime(), dto.getEndTime());

        User createUser = new User();
        createUser.setUsername(principal.getName());
        dto.setCreateUser(createUser);
        reservationService.isReservationExistWithGivenReservation(dto);
        reservationService.addReservation(dto);

        sendSpecialEmailJob(dto);
    }

    @PutMapping("/reservations")
    @PreAuthorize("hasRole('ROLE_USER')")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void updateReservation(@RequestBody ReservationDTO dto,
                                  Principal principal) throws ReservationCredentialsException {
        if (ValidationUtil.isNullOrEmpty(dto.getTopic()) || ValidationUtil.isNull(dto.getId(), dto.getDate(), dto.getStartTime(), dto.getEndTime(), dto.getRoomId())) {
            throw new ReservationCredentialsException(MessageConstants.ERROR_MESSAGE_ONE_OR_MORE_FIELDS_ARE_EMPTY);
        }
        TimeUtil.checkTime(dto.getStartTime(), dto.getEndTime());

        User createUser = new User();
        createUser.setUsername(principal.getName());
        dto.setCreateUser(createUser);
        reservationService.isReservationExistWithGivenReservation(dto);
        reservationService.updateReservation(dto);
    }

    @GetMapping("/reservations/{reservationId}/participants")
    @PreAuthorize("hasRole('ROLE_USER')")
    public GenericResponse findUsersOfReservationById(@PathVariable(name = "reservationId", required = false) Long id) throws ReservationCredentialsException {
        reservationService.isReservationExistWithGivenId(id);
        List<User> users = reservationService.findUsersOfReservationById(id);
        return GenericResponse.withSuccess(HttpStatus.OK, "participants of specific reservation", users);
    }

    @DeleteMapping("/reservations/{reservationId}")
    @PreAuthorize("hasRole('ROLE_USER')")
    @ResponseStatus(HttpStatus.OK)
    public void deleteReservation(@PathVariable(name = "reservationId", required = false) Long id,
                                  Principal principal) throws ReservationCredentialsException {
        if (ValidationUtil.isNull(id)) {
            throw new ReservationCredentialsException(MessageConstants.ERROR_MESSAGE_ONE_OR_MORE_FIELDS_ARE_EMPTY);
        }
        reservationService.isReservationExistWithGivenId(id);

        Reservation reservation = new Reservation();
        User user = new User();
        user.setUsername(principal.getName());
        reservation.setId(id);
        reservation.setUser(user);

        reservationService.deleteReservation(reservation);
    }

    @GetMapping("/reservations/{reservationId}")
    @PreAuthorize("hasRole('ROLE_USER')")
    public GenericResponse findReservationById(@PathVariable(name = "reservationId", required = false) Long id) throws ReservationCredentialsException {
        if (ValidationUtil.isNull(id)) {
            throw new ReservationCredentialsException(MessageConstants.ERROR_MESSAGE_ONE_OR_MORE_FIELDS_ARE_EMPTY);
        }
        reservationService.isReservationExistWithGivenId(id);

        ReservationDTO reservation = reservationService.findReservationById(id);
        List<User> users = reservationService.findUsersOfReservationById(id);
        reservation.setParticipants(users);

        return GenericResponse.withSuccess(HttpStatus.OK, "specific reservation by id", reservation);
    }

//    //private methods
//    private void sendEmailJob(ReservationDTO dto) {
//        if (dto.getParticipants() != null && dto.getParticipants().size() != 0) {
//            ExecutorService service = Executors.newSingleThreadExecutor();
//
//            Runnable runnableTask = () -> {
//                dto.getParticipants().forEach(user -> {
//                    emailSenderUtil.sendEmailMessage(user.getUsername(), "Azərbaycan Dövlət Reklam Agentliyi (Reservasiya bildirişi)",
//                            "Mövzu: " + dto.getTopic() + "\n" +
//                                    "Tarix: " + dto.getDate().toString() + "\n" +
//                                    "Başlama saatı: " + dto.getStartTime().toString() + "\n" +
//                                    "Bitmə saatı: " + dto.getEndTime().toString() + "\n" +
//                                    "Otaq: " + dto.getRoom().getId());
//                });
//            };
//            service.submit(runnableTask);
//            service.shutdown();
//        }
//    }


    private void sendSpecialEmailJob(ReservationDTO dto) {
        ExecutorService service = Executors.newSingleThreadExecutor();

        List<String> list = new ArrayList<>();
        String user1 = dto.getCreateUser().getUsername();
        String user2 = "senan0144@gmail.com";

        list.add(user1);
        list.add(user2);

        Runnable runnableTask = () -> {
            list.forEach(user -> {
                emailSenderUtil.sendEmailMessage(user, "Azərbaycan Dövlət Reklam Agentliyi (Reservasiya bildirişi)",
                        "Mövzu: " + dto.getTopic() + "\n" +
                                "Tarix: " + dto.getDate().toString() + "\n" +
                                "Başlama saatı: " + dto.getStartTime().toString() + "\n" +
                                "Bitmə saatı: " + dto.getEndTime().toString() + "\n" +
                                "Otaq: " + dto.getRoomId());
            });
        };
        service.submit(runnableTask);
        service.shutdown();
    }

}
