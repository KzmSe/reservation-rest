package az.gov.adra.repository;

import az.gov.adra.entity.Room;
import az.gov.adra.repository.interfaces.RoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

@Repository
public class RoomRepositoryImpl implements RoomRepository {

    private static final String FIND_ALL_ROOMS = "select * from Room";

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Override
    public List<Room> findAllRooms() {
        List<Room> rooms = jdbcTemplate.query(FIND_ALL_ROOMS, new ResultSetExtractor<List<Room>>() {
            @Override
            public List<Room> extractData(ResultSet rs) throws SQLException, DataAccessException {
                List<Room> list = new LinkedList<>();
                while(rs.next()){
                    Room room = new Room();
                    room.setId(rs.getInt("id"));
                    room.setName(rs.getString("name"));
                    list.add(room);
                }
                return list;
            }
        });
        return rooms;
    }

}
