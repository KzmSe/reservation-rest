package az.gov.adra.mappers;

import az.gov.adra.dataTransferObjects.ReservationForUpdateDTO;
import az.gov.adra.entity.Reservation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface ReservationMapper {

//    ReservationMapper INSTANCE = Mappers.getMapper(ReservationMapper.class);
//
//    @Mappings({
//            @Mapping(source = "id", target = "id"),
//            @Mapping(source = "employee", target = "employee"),
//            @Mapping(source = "fullname", target = "fullname"),
//            @Mapping(source = "topic", target = "topic"),
//            @Mapping(source = "date", target = "date"),
//            @Mapping(source = "startTime", target = "startTime"),
//            @Mapping(source = "endTime", target = "endTime"),
//            @Mapping(source = "room", target = "room"),
//            @Mapping(source = "status", target = "status"),
//            @Mapping(source = "status2", target = "status2")
//    })
//    ReservationForUpdateDTO reservationToReservationForUpdateDTO(Reservation reservation);

}
