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
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.mail.Address;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RestController
public class ReservationController {

    @Autowired
    private ReservationService reservationService;
    @Autowired
    private EmailSenderUtil emailSenderUtil;
    @Value("${spring.mail.subject}")
    private String subject;


    @GetMapping("/reservations")
    @ApiOperation(value = "Finds all reservations")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_HR')")
    public GenericResponse findAllReservations(@RequestParam(name = "status", required = false) Integer status,
                                               @RequestParam(name = "fetchNext", required = false) Integer fetchNext) throws ReservationCredentialsException {
        if (ValidationUtil.isNull(status) || ValidationUtil.isNull(fetchNext)) {
            throw new ReservationCredentialsException(MessageConstants.ERROR_MESSAGE_ONE_OR_MORE_FIELDS_ARE_EMPTY);
        }

        List<ReservationDTO> reservations = reservationService.findAllReservationsByStatus(status, fetchNext);
        return GenericResponse.withSuccess(HttpStatus.OK, "list of reservations", reservations);
    }

    @GetMapping("/reservations/me")
    @ApiOperation(value = "Finds my reservations")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_HR')")
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
    @ApiOperation(value = "Finds reservations which I invented")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_HR')")
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
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_HR')")
    @ApiOperation(value = "Adds reservation")
    @ResponseStatus(HttpStatus.CREATED)
    public void addReservation(@RequestBody ReservationDTO dto,
                               Principal principal) throws ReservationCredentialsException, AddressException {
        if (ValidationUtil.isNullOrEmpty(dto.getTopic()) || ValidationUtil.isNull(dto.getDate(), dto.getStartTime(), dto.getEndTime(), dto.getRoomId())) {
            throw new ReservationCredentialsException(MessageConstants.ERROR_MESSAGE_ONE_OR_MORE_FIELDS_ARE_EMPTY);
        }
        TimeUtil.checkTime(dto.getStartTime(), dto.getEndTime());

        User createUser = new User();
        createUser.setUsername(principal.getName());
        dto.setCreateUser(createUser);
        reservationService.isReservationExistWithGivenDateAndTime(dto);
        reservationService.addReservation(dto);

        sendReservationEmail(dto);
    }

    @PutMapping("/reservations")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_HR')")
    @ApiOperation(value = "Updates reservation")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void updateReservation(@RequestBody ReservationDTO dto,
                                  Principal principal) throws ReservationCredentialsException {
        //validation
        if (ValidationUtil.isNullOrEmpty(dto.getTopic()) || ValidationUtil.isNull(dto.getId(), dto.getDate(), dto.getStartTime(), dto.getEndTime(), dto.getRoomId())) {
            throw new ReservationCredentialsException(MessageConstants.ERROR_MESSAGE_ONE_OR_MORE_FIELDS_ARE_EMPTY);
        }

        //check time
        TimeUtil.checkTime(dto.getStartTime(), dto.getEndTime());

        //get current reservation from db
        ReservationDTO currentReservation = reservationService.findReservationById(dto.getId());

        //create flag(hasChanged)
        boolean hasChanged = false;
        if (!dto.getDate().isEqual(currentReservation.getDate()) || !dto.getStartTime().equals(currentReservation.getStartTime()) || !dto.getEndTime().equals(currentReservation.getEndTime()) || dto.getRoomId().compareTo(currentReservation.getRoom().getId()) != 0) {
            hasChanged = true;
        }

        //check if reservation has changed or not
        if (hasChanged) {
            reservationService.isReservationExistWithGivenDateAndTime(dto);
        }

        User createUser = new User();
        createUser.setUsername(principal.getName());
        dto.setCreateUser(createUser);

        reservationService.updateReservation(dto);
    }

    @GetMapping("/reservations/{reservationId}/participants")
    @ApiOperation(value = "Finds participants of reservation")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_HR')")
    public GenericResponse findUsersOfReservationById(@PathVariable(name = "reservationId", required = false) Long id) throws ReservationCredentialsException {
        reservationService.isReservationExistWithGivenId(id);
        List<User> users = reservationService.findUsersOfReservationById(id);
        return GenericResponse.withSuccess(HttpStatus.OK, "participants of specific reservation", users);
    }

    @DeleteMapping("/reservations/{reservationId}")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_HR')")
    @ApiOperation(value = "Deletes reservation")
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
    @ApiOperation(value = "Finds specific reservation")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_HR')")
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


    //private methods
    private void sendReservationEmail(ReservationDTO dto) throws AddressException {
        ExecutorService service = Executors.newSingleThreadExecutor();

        List<Address> recipients = new ArrayList<>();
        Address principal = new InternetAddress(dto.getCreateUser().getUsername());

        dto.getParticipants().forEach(user -> {
            if (!user.getUsername().isEmpty() && user.getUsername().contains("@")) {
                try {
                    recipients.add(new InternetAddress(user.getUsername()));

                } catch (AddressException e) {
                    e.printStackTrace();
                }
            }
        });

        //Address participant1 = new InternetAddress("v.namazov@adra.gov.az");
        //Address participant2 = new InternetAddress("n.nasirova@adra.gov.az");
        //Address participant3 = new InternetAddress("e.mardanov@adra.gov.az");
        //Address participant4 = new InternetAddress("gunel.hasanova@inno.az");

        recipients.add(principal);
        //recipients.add(participant1);
        //recipients.add(participant2);
        //recipients.add(participant3);
        //recipients.add(participant4);

        Runnable runnableTask = () -> {
            recipients.forEach(add -> {
                emailSenderUtil.sendEmailMessage(add, subject,
                          "Rezerv edən (email): " + dto.getCreateUser().getUsername() + "\n" +
                                "Mövzu: " + dto.getTopic() + "\n" +
                                "Tarix: " + dto.getDate().toString() + "\n" +
                                "Başlama saatı: " + dto.getStartTime().toString() + "\n" +
                                "Bitmə saatı: " + dto.getEndTime().toString() + "\n" +
                                "Otaq: " + dto.getRoomName());
            });
        };

        service.submit(runnableTask);
        service.shutdown();
    }

//    @PostMapping("/feedback")
//    public void sendFeedback(@RequestBody Feedback feedback, BindingResult bindingResult) throws ValidationException {
//        //Validation
//        if (bindingResult.hasErrors()) {
//            throw new ValidationException("Feedback is not valid");
//        }
//
//        //Create a email sender
//        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
//        mailSender.setHost(this.emailConfig.getHost());
//        mailSender.setPort(this.emailConfig.getPort());
//        mailSender.setUsername(this.emailConfig.getUsername());
//        mailSender.setPassword(this.emailConfig.getPassword());
//
////        //Create an email instance
////        SimpleMailMessage mailMessage = new SimpleMailMessage();
////        mailMessage.setFrom(feedback.getEmail());
////        mailMessage.setTo("senan0144@gmail.com");
////        mailMessage.setSubject("Feedback from: " + feedback.getName());
////        mailMessage.setText(feedback.getFeedback());
//
//        //Create an email instance
//
//        //Send email
//        mailSender.send(new MimeMessagePreparator() {
//            @Override
//            public void prepare(MimeMessage mimeMessage) throws Exception {
//                Address address = new InternetAddress("senan0144@gmail.com");
//                mimeMessage.setFrom(feedback.getEmail());
//                mimeMessage.setRecipient(Message.RecipientType.TO, address);
//                mimeMessage.setSubject("Feedback from: " + feedback.getName());
//                mimeMessage.setText(feedback.getFeedback());
//
//            }
//        });
//    }

}
