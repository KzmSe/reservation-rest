package az.gov.adra.dataTransferObjects;

import az.gov.adra.entity.Employee;
import az.gov.adra.entity.Room;
import az.gov.adra.entity.User;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;
import java.time.LocalTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReservationDTO {

    private Integer rowNumber;
    private Long id;
    private String topic;
    private Date date;
    private LocalTime startTime;
    private LocalTime endTime;
    private Room room;
    private User user;


}
