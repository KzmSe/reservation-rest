package az.gov.adra.controller;

import az.gov.adra.entity.Room;
import az.gov.adra.entity.response.GenericResponse;
import az.gov.adra.service.interfaces.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class RoomController {

    @Autowired
    private RoomService roomService;


    @GetMapping("/rooms")
    @PreAuthorize("hasRole('ROLE_USER')")
    public GenericResponse findAllRooms() {
        List<Room> rooms = roomService.findAllRooms();
        return GenericResponse.withSuccess(HttpStatus.OK, "list of all rooms", rooms);
    }

}
