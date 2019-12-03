package az.gov.adra.dataTransferObjects;

import az.gov.adra.entity.Room;
import az.gov.adra.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReservationDTOForAdd {

    private String topic;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private Room room;
    private List<User> users;

}
