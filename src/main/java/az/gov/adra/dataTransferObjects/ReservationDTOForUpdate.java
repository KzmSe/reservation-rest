package az.gov.adra.dataTransferObjects;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReservationDTOForUpdate {

    private Long id;
    private String topic;
    private LocalDate date;
    private String createUser;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer roomId;
    private Integer status;
    private String[] users;

}
