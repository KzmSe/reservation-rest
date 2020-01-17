package az.gov.adra.dataTransferObjects;

import az.gov.adra.entity.Room;
import az.gov.adra.entity.User;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReservationDTO {

    private Integer rowNumber;
    private Long id;
    private String topic;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private User createUser;
    private List<User> participants;
    private Room room;
    private Integer roomId;
    private String roomName;
    private Integer status;

}

