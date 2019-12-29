package az.gov.adra.repository.interfaces;

import az.gov.adra.entity.Room;

import java.util.List;

public interface RoomRepository {

    List<Room> findAllRooms();

}
