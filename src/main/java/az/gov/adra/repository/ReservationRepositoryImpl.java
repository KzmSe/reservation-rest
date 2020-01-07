package az.gov.adra.repository;

import az.gov.adra.constant.MessageConstants;
import az.gov.adra.constant.ReservationConstants;
import az.gov.adra.dataTransferObjects.ReservationDTO;
import az.gov.adra.entity.*;
import az.gov.adra.exception.ReservationCredentialsException;
import az.gov.adra.repository.interfaces.ReservationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

@Repository
public class ReservationRepositoryImpl implements ReservationRepository {

    private static final String FIND_ALL_RESERVATIONS_BY_STATUS_SQL = "select ROW_NUMBER() OVER (ORDER BY res.date) AS number, res.id, res.topic, res.date, res.start_time, res.end_time, room.name as room, u.name, u.surname, u.username from Reservation res inner join Room room on res.room_id = room.id inner join users u on res.username = u.username where res.status = ? order by res.date, res.start_time OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
    private static final String FIND_MY_RESERVATIONS_SQL = "select ROW_NUMBER() OVER (ORDER BY res.date) AS number, res.id, res.topic, res.date, res.start_time, res.end_time, room.name as room from Reservation res inner join Room room on res.room_id = room.id where res.username = ? and res.status = ? order by res.date, res.start_time OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
    private static final String FIND_RESERVATIONS_WHICH_I_JOINED_SQL = "select ROW_NUMBER() OVER (ORDER BY re.date) AS number, re.id, re.topic, re.date, re.start_time, re.end_time, room.name as room, u.name, u.surname, u.username from Reservation re inner join Reservation_users ru on re.id = ru.reservation_id inner join Room room on re.room_id = room.id inner join users u on re.username = u.username where ru.username = ? and re.status = ? order by re.date, res.start_time OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
    private static final String ADD_RESERVATION_SQL = "insert into Reservation(username, topic, date, start_time, end_time, room_id, status) values (?, ?, ?, ?, ?, ?, ?)";
    private static final String UPDATE_RESERVATION_SQL = "update Reservation set topic = ?, date = ?, start_time = ?, end_time = ?, room_id = ? where id = ? and username = ? and status = ?";
    private static final String DELETE_USERS_OF_RESERVATION_SQL = "delete from Reservation_users where reservation_id = ?";
    private static final String FIND_USERS_OF_RESERVATION_BY_ID = "select u.username, u.name, u.surname from Reservation_users ru inner join users u on ru.username = u.username where ru.reservation_id = ?";
    private static final String ADD_USERS_OF_RESERVATION_SQL = "insert into Reservation_users(username, reservation_id) values(?, ?)";
    private static final String IS_RESERVATION_EXIST_WITH_GIVEN_ID_SQL = "select count(*) as count from Reservation where id = ? and status = ?";
    private static final String IS_RESERVATION_EXIST_WITH_GIVEN_DATE_AND_TIME_SQL = "select count(*) as count from Reservation where ((? between start_time and end_time or ? between start_time and end_time) or (start_time between ? and ? or end_time between ? and ?)) and date = ? and room_id = ? and status = ?";
    private static final String DELETE_RESERVATION_SQL = "update Reservation set status = ? where id = ? and username = ?";
    private static final String FIND_RESERVATION_BY_ID_SQL = "select re.topic, re.date, re.start_time, re.end_time, ro.id as room_id, ro.name as room_name from Reservation re inner join Room ro on re.room_id = ro.id where re.id = ? and status = ?";
    private static final String UPDATE_RESERVATION_STATUS_SQL = "update Reservation set status = ? where date <= CONVERT(DATE, GETDATE()) and end_time <= CONVERT(time, CURRENT_TIMESTAMP) and status = ?";


    @Autowired
    JdbcTemplate jdbcTemplate;

    @Override
    public List<ReservationDTO> findAllReservationsByStatus(int status, int fetchNext) {
        List<ReservationDTO> reservationList = jdbcTemplate.query(FIND_ALL_RESERVATIONS_BY_STATUS_SQL, new Object[]{status, ReservationConstants.RESERVATION_OFFSET, fetchNext}, new ResultSetExtractor<List<ReservationDTO>>() {
            @Override
            public List<ReservationDTO> extractData(ResultSet rs) throws SQLException, DataAccessException {
                List<ReservationDTO> list = new LinkedList<>();

                while (rs.next()) {
                    ReservationDTO reservation = new ReservationDTO();
                    reservation.setRowNumber(rs.getInt("number"));
                    reservation.setId(rs.getLong("id"));
                    reservation.setTopic(rs.getString("topic"));
                    reservation.setDate(rs.getDate("date").toLocalDate());
                    reservation.setStartTime(rs.getTime("start_time").toLocalTime());
                    reservation.setEndTime(rs.getTime("end_time").toLocalTime());

                    Room room = new Room();
                    room.setName(rs.getString("room"));

                    User user = new User();
                    user.setName(rs.getString("name"));
                    user.setSurname(rs.getString("surname"));
                    user.setUsername(rs.getString("username"));

                    reservation.setRoom(room);
                    reservation.setCreateUser(user);

                    list.add(reservation);
                }
                return list;
            }
        });

        return reservationList;
    }

    @Override
    public List<ReservationDTO> findMyReservations(String username, int status, int fetchNext) {
        List<ReservationDTO> reservationList = jdbcTemplate.query(FIND_MY_RESERVATIONS_SQL, new Object[]{username, status, ReservationConstants.RESERVATION_OFFSET, fetchNext}, new ResultSetExtractor<List<ReservationDTO>>() {
            @Override
            public List<ReservationDTO> extractData(ResultSet rs) throws SQLException, DataAccessException {
                List<ReservationDTO> list = new LinkedList<>();

                while (rs.next()) {
                    ReservationDTO reservation = new ReservationDTO();
                    reservation.setRowNumber(rs.getInt("number"));
                    reservation.setId(rs.getLong("id"));
                    reservation.setTopic(rs.getString("topic"));
                    reservation.setDate(rs.getDate("date").toLocalDate());
                    reservation.setStartTime(rs.getTime("start_time").toLocalTime());
                    reservation.setEndTime(rs.getTime("end_time").toLocalTime());

                    Room room = new Room();
                    room.setName(rs.getString("room"));

                    reservation.setRoom(room);

                    list.add(reservation);
                }
                return list;
            }
        });

        return reservationList;
    }

    @Override
    public List<ReservationDTO> findReservationsWhichIJoined(String username, int status, int fetchNext) {
        List<ReservationDTO> reservationList = jdbcTemplate.query(FIND_RESERVATIONS_WHICH_I_JOINED_SQL, new Object[]{username, status, ReservationConstants.RESERVATION_OFFSET, fetchNext}, new ResultSetExtractor<List<ReservationDTO>>() {
            @Override
            public List<ReservationDTO> extractData(ResultSet rs) throws SQLException, DataAccessException {
                List<ReservationDTO> list = new LinkedList<>();

                while (rs.next()) {
                    ReservationDTO reservation = new ReservationDTO();
                    reservation.setRowNumber(rs.getInt("number"));
                    reservation.setId(rs.getLong("id"));
                    reservation.setTopic(rs.getString("topic"));
                    reservation.setDate(rs.getDate("date").toLocalDate());
                    reservation.setStartTime(rs.getTime("start_time").toLocalTime());
                    reservation.setEndTime(rs.getTime("end_time").toLocalTime());

                    Room room = new Room();
                    room.setName(rs.getString("room"));

                    User user = new User();
                    user.setName(rs.getString("name"));
                    user.setSurname(rs.getString("surname"));
                    user.setUsername(rs.getString("username"));

                    reservation.setCreateUser(user);

                    reservation.setRoom(room);

                    list.add(reservation);
                }
                return list;
            }
        });

        return reservationList;
    }

    @Override
    public void addReservation(ReservationDTO dto) throws ReservationCredentialsException {
        //add reservation
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(new PreparedStatementCreator() {
            @Override
            public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
                PreparedStatement ps = connection.prepareStatement(ADD_RESERVATION_SQL, new String[] {"id"});
                ps.setString(1, dto.getCreateUser().getUsername());
                ps.setString(2, dto.getTopic());
                ps.setString(3, dto.getDate().toString());
                ps.setString(4, dto.getStartTime().toString());
                ps.setString(5, dto.getEndTime().toString());
                //ps.setLong(6, dto.getRoom().getId());
                ps.setLong(6, dto.getRoomId());
                ps.setInt(7, ReservationConstants.RESERVATION_STATUS_ACTIVE);

                return ps;
            }
        }, keyHolder);

//        //in addition to add reservation, add users of reservation
//        dto.getParticipants().forEach(user -> {
//            jdbcTemplate.update(ADD_USERS_OF_RESERVATION_SQL, user.getUsername(), keyHolder.getKey());
//        });
    }

    @Override
    public void updateReservation(ReservationDTO dto) throws ReservationCredentialsException {
        //update reservation
        int affectedRows = jdbcTemplate.update(UPDATE_RESERVATION_SQL, dto.getTopic(), dto.getDate(), dto.getStartTime(), dto.getEndTime(), dto.getRoomId(), dto.getId(), dto.getCreateUser().getUsername(), ReservationConstants.RESERVATION_STATUS_ACTIVE);
        if (affectedRows == 0) {
            throw new ReservationCredentialsException(MessageConstants.ERROR_MESSAGE_INTERNAL_ERROR);
        }

//        //in addition to update reservation, update users of reservation
//        jdbcTemplate.update(DELETE_USERS_OF_RESERVATION_SQL, dto.getId());
//
//        dto.getParticipants().forEach(user -> {
//            jdbcTemplate.update(ADD_USERS_OF_RESERVATION_SQL, user.getUsername(), dto.getId());
//        });
    }

    @Override
    public List<User> findUsersOfReservationById(long id) {
        List<User> users = jdbcTemplate.query(FIND_USERS_OF_RESERVATION_BY_ID, new Object[]{id}, new ResultSetExtractor<List<User>>() {
            @Override
            public List<User> extractData(ResultSet rs) throws SQLException, DataAccessException {
                List<User> list = new LinkedList<>();

                while (rs.next()) {
                    User user = new User();
                    user.setUsername(rs.getString("username"));
                    user.setName(rs.getString("name"));
                    user.setSurname(rs.getString("surname"));
                    list.add(user);
                }
                return list;
            }
        });

        return users;
    }

    @Override
    public void isReservationExistWithGivenId(long id) throws ReservationCredentialsException {
        int count = jdbcTemplate.queryForObject(IS_RESERVATION_EXIST_WITH_GIVEN_ID_SQL, new Object[] {id, ReservationConstants.RESERVATION_STATUS_ACTIVE}, Integer.class);
        if (count <= 0) {
            throw new ReservationCredentialsException(MessageConstants.ERROR_MESSAGE_RESERVATION_NOT_FOUND);
        }
    }

    @Override
    public void isReservationExistWithGivenDateAndTime(ReservationDTO dto) throws ReservationCredentialsException {
        int count = jdbcTemplate.queryForObject(IS_RESERVATION_EXIST_WITH_GIVEN_DATE_AND_TIME_SQL, new Object[] {dto.getStartTime().toString(), dto.getEndTime().toString(), dto.getStartTime().toString(), dto.getEndTime().toString(), dto.getStartTime().toString(), dto.getEndTime().toString(), dto.getDate().toString(), dto.getRoomId(), ReservationConstants.RESERVATION_STATUS_ACTIVE}, Integer.class);
        if (count > 0) {
            throw new ReservationCredentialsException(MessageConstants.ERROR_MESSAGE_RESERVATION_ALREADY_EXIST);
        }
    }

    @Override
    public void deleteReservation(Reservation reservation) throws ReservationCredentialsException {
        int affectedRows = jdbcTemplate.update(DELETE_RESERVATION_SQL, ReservationConstants.RESERVATION_STATUS_DELETED, reservation.getId(), reservation.getUser().getUsername());
        if (affectedRows == 0) {
            throw new ReservationCredentialsException(MessageConstants.ERROR_MESSAGE_INTERNAL_ERROR);
        }
    }

    @Override
    public ReservationDTO findReservationById(long id) {
        ReservationDTO reservation = jdbcTemplate.query(FIND_RESERVATION_BY_ID_SQL, new Object[]{id, ReservationConstants.RESERVATION_STATUS_ACTIVE}, new ResultSetExtractor<ReservationDTO>() {
            @Override
            public ReservationDTO extractData(ResultSet rs) throws SQLException, DataAccessException {
                ReservationDTO dto = new ReservationDTO();

                while (rs.next()) {
                    dto.setTopic(rs.getString("topic"));
                    dto.setDate(rs.getDate("date").toLocalDate());
                    dto.setStartTime(rs.getTime("start_time").toLocalTime());
                    dto.setEndTime(rs.getTime("end_time").toLocalTime());

                    Room room = new Room();
                    room.setId(rs.getInt("room_id"));
                    room.setName(rs.getString("room_name"));

                    dto.setRoom(room);
                }
                return dto;
            }
        });

        return reservation;
    }

    @Override
    public void updateReservationStatus() {
        jdbcTemplate.update(UPDATE_RESERVATION_STATUS_SQL, ReservationConstants.RESERVATION_STATUS_FINISHED, ReservationConstants.RESERVATION_STATUS_ACTIVE);
    }

}
