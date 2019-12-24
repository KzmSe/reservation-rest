package az.gov.adra.controller;

import az.gov.adra.constant.MessageConstants;
import az.gov.adra.dataTransferObjects.ReservationDTO;
import az.gov.adra.dataTransferObjects.ReservationDTOForAdd;
import az.gov.adra.dataTransferObjects.ReservationDTOForUpdate;
import az.gov.adra.entity.Reservation;
import az.gov.adra.entity.User;
import az.gov.adra.entity.response.GenericResponse;
import az.gov.adra.exception.ReservationCredentialsException;
import az.gov.adra.service.interfaces.ReservationService;
import az.gov.adra.util.ValidationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

import static jdk.nashorn.internal.runtime.Debug.id;

@RestController
public class ReservationController {

    @Autowired
    private ReservationService reservationService;


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
    public void addReservation(@RequestBody ReservationDTOForAdd dto,
                               Principal principal) throws ReservationCredentialsException {
        dto.setCreateUser(principal.getName());
        //reservationService.isReservationExistWithGivenReservation(dto);
        reservationService.addReservation(dto);
    }

    @PutMapping("/reservations")
    @PreAuthorize("hasRole('ROLE_USER')")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void updateReservation(@RequestBody ReservationDTOForUpdate dto,
                                  Principal principal) throws ReservationCredentialsException {
        dto.setCreateUser(principal.getName());
        //reservationService.isReservationExistWithGivenReservation(dto);
        reservationService.updateReservation(dto);
    }

    @GetMapping("/reservations/{reservationId}/users")
    @PreAuthorize("hasRole('ROLE_USER')")
    public List<User> findUsersOfReservationById(@PathVariable(name = "reservationId", required = false) Long id) throws ReservationCredentialsException {
        reservationService.isReservationExistWithGivenId(id);
        List<User> users = reservationService.findUsersOfReservationById(id);
        return users;
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

}
