package az.gov.adra.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Reservation {

    private Long id;
    private User user;
    private String topic;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private Room room;
    private Integer status;

}

