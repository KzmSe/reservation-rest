package az.gov.adra.service;

import az.gov.adra.entity.Room;
import az.gov.adra.repository.interfaces.RoomRepository;
import az.gov.adra.service.interfaces.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoomServiceImpl implements RoomService {

    private RoomRepository roomRepository;

    @Autowired
    public RoomServiceImpl(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }


    @Override
    public List<Room> findAllRooms() {
        return null;
    }

}
