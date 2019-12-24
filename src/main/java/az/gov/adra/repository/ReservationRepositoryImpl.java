package az.gov.adra.repository;

import az.gov.adra.constant.MessageConstants;
import az.gov.adra.constant.ReservationConstants;
import az.gov.adra.dataTransferObjects.ReservationDTO;
import az.gov.adra.dataTransferObjects.ReservationDTOForAdd;
import az.gov.adra.dataTransferObjects.ReservationDTOForUpdate;
import az.gov.adra.entity.*;
import az.gov.adra.exception.ReservationCredentialsException;
import az.gov.adra.repository.interfaces.ReservationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
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

    private static final String FIND_ALL_RESERVATIONS_BY_STATUS_SQL = "select ROW_NUMBER() OVER (ORDER BY (res.date) desc) AS number, res.id, res.topic, res.date, res.start_time, res.end_time, room.name as room, u.name, u.surname, u.username from Reservation res inner join Room room on res.room_id = room.id inner join users u on res.username = u.username where res.status = ? order by res.date desc OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
    private static final String FIND_MY_RESERVATIONS_SQL = "select ROW_NUMBER() OVER (ORDER BY (res.date) desc) AS number, res.id, res.topic, res.date, res.start_time, res.end_time, room.name as room from Reservation res inner join Room room on res.room_id = room.id where res.username = ? and res.status = ? order by res.date desc OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
    private static final String FIND_RESERVATIONS_WHICH_I_JOINED_SQL = "select ROW_NUMBER() OVER (ORDER BY (re.date) desc) AS number, re.id, re.topic, re.date, re.start_time, re.end_time, room.name as room, u.name, u.surname, u.username from Reservation re inner join Reservation_users ru on re.id = ru.reservation_id inner join Room room on re.room_id = room.id inner join users u on re.username = u.username where ru.username = ? and re.status = ? order by re.date desc OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
    private static final String ADD_RESERVATION_SQL = "insert into Reservation(username, topic, date, start_time, end_time, room_id, status) values (?, ?, ?, ?, ?, ?, ?)";
    private static final String UPDATE_RESERVATION_SQL = "update Reservation set topic = ?, date = ?, start_time = ?, end_time = ?, room_id = ? where id = ? and username = ? and status = ?";
    private static final String DELETE_USERS_OF_RESERVATION_SQL = "delete from Reservation_users where reservation_id = ?";
    private static final String ADD_USERS_OF_RESERVATION_SQL = "insert into Reservation_users(username, reservation_id) values(?, ?)";
    private static final String IS_RESERVATION_EXIST_WITH_GIVEN_ID_SQL = "select count(*) as count from Reservation where id = ? and status = ?";
    private static final String IS_RESERVATION_EXIST_WITH_GIVEN_RESERVATION_SQL = "select count(*) as count from Reservation where ((? between start_time and end_time or ? between start_time and end_time) or (start_time between ? and ? or end_time between ? and ?)) and date = ? and room_id = ? and status = ?";
    private static final String DELETE_RESERVATION_SQL = "update Reservation set status = ? where id = ? and username = ?";

    private static final String findReservationByIdSql = "select res.id as reservation_id, res.fullname, res.topic, res.date, res.start_time, res.end_time, room.name, res.status, room.id as room_id, room.name from Reservation res inner join Room room on res.room_id = room.id where res.id = ?";
    private static final String findReservationsByEmployeeIdSql = "select res.id as reservation_id, res.fullname, res.topic, res.date, res.start_time, res.end_time, room.id as room_id, room.name, res.status, res.status2, ROW_NUMBER() OVER (ORDER BY (res.date) desc) AS number from Reservation res inner join Room room on res.room_id = room.id where res.employee_id = ? and ((res.status = ? and res.status2 = ?) or (status = ? and res.status2 = ?)) order by res.date desc OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
    private static final String updateReservationSql = "update Reservation set fullname = ?, topic = ?, date = ?, start_time = ?, end_time = ?, room_id = ? where id = ?";
    private static final String deleteReservationByIdSql = "update Reservation set status = ? where id = ? and employee_id = ?";
    private static final String isReservationExistSql = "select count(*) as count from Reservation where ((? between start_time and end_time or ?  between start_time and end_time) or (start_time between  ? and ? or end_time between  ? and ?)) and date = ? and room_id = ? and status = ?";
    private static final String findReservationsByStatusAndStatus2Sql = "select res.id as reservation_id, emp.id as employee_id, per.name, per.surname, res.fullname, res.topic, res.date, res.start_time, res.end_time, room.id as room_id, room.name as room_name, res.status, res.status2, ROW_NUMBER() OVER (ORDER BY (res.date) desc) AS number from Reservation res inner join Employee emp on res.employee_id = emp.id inner join Person per on emp.person_id = per.id inner join Room room on res.room_id = room.id where res.date >= (GETDATE() - ?) and res.status = ? and res.status2 = ? order by res.date desc OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
    private static final String updateReservationStatusAndStatus2Sql = "update Reservation set status = ?, status2 = ? where id = ? and employee_id = ?";
    private static final String updateReservationStatusSql = "update Reservation set status = ?, status2 = ? where date <= CONVERT(DATE, GETDATE()) and end_time <= CONVERT(time, CURRENT_TIMESTAMP) and status = ?";

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
                    reservation.setDate(rs.getDate("date"));
                    reservation.setStartTime(rs.getTime("start_time").toLocalTime());
                    reservation.setEndTime(rs.getTime("end_time").toLocalTime());

                    Room room = new Room();
                    room.setName(rs.getString("room"));

                    User user = new User();
                    user.setName(rs.getString("name"));
                    user.setSurname(rs.getString("surname"));
                    user.setUsername(rs.getString("username"));

                    reservation.setRoom(room);
                    reservation.setUser(user);

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
                    reservation.setDate(rs.getDate("date"));
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
                    reservation.setDate(rs.getDate("date"));
                    reservation.setStartTime(rs.getTime("start_time").toLocalTime());
                    reservation.setEndTime(rs.getTime("end_time").toLocalTime());

                    Room room = new Room();
                    room.setName(rs.getString("room"));

                    User user = new User();
                    user.setName(rs.getString("name"));
                    user.setSurname(rs.getString("surname"));
                    user.setUsername(rs.getString("username"));

                    reservation.setUser(user);

                    reservation.setRoom(room);

                    list.add(reservation);
                }
                return list;
            }
        });

        return reservationList;
    }

    @Override
    public void addReservation(ReservationDTOForAdd dto) throws ReservationCredentialsException {
        //add reservation
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(new PreparedStatementCreator() {
            @Override
            public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
                PreparedStatement ps = connection.prepareStatement(ADD_RESERVATION_SQL, new String[] {"id"});
                ps.setString(1, dto.getCreateUser());
                ps.setString(2, dto.getTopic());
                ps.setString(3, dto.getDate().toString());
                ps.setString(4, dto.getStartTime().toString());
                ps.setString(5, dto.getEndTime().toString());
                ps.setLong(6, dto.getRoomId());
                ps.setInt(7, dto.getStatus());

                return ps;
            }
        }, keyHolder);

        //in addition to add reservation, add users of reservation
        Arrays.stream(dto.getUsers()).forEach(username -> {
            jdbcTemplate.update(ADD_USERS_OF_RESERVATION_SQL, username, keyHolder.getKey());
        });
    }

    @Override
    public void updateReservation(ReservationDTOForUpdate dto) throws ReservationCredentialsException {
        //update reservation
        int affectedRows = jdbcTemplate.update(UPDATE_RESERVATION_SQL, dto.getTopic(), dto.getDate(), dto.getStartTime(), dto.getEndTime(), dto.getRoomId(), dto.getId(), dto.getCreateUser(), dto.getStatus());
        if (affectedRows == 0) {
            throw new ReservationCredentialsException(MessageConstants.ERROR_MESSAGE_INTERNAL_ERROR);
        }

        //in addition to update reservation, update users of reservation
        jdbcTemplate.update(DELETE_USERS_OF_RESERVATION_SQL, dto.getId());

        Arrays.stream(dto.getUsers()).forEach(username -> {
            jdbcTemplate.update(ADD_USERS_OF_RESERVATION_SQL, username, dto.getId());
        });
    }

    @Override
    public void isReservationExistWithGivenId(long id) throws ReservationCredentialsException {
        int count = jdbcTemplate.queryForObject(IS_RESERVATION_EXIST_WITH_GIVEN_ID_SQL, new Object[] {id, ReservationConstants.RESERVATION_STATUS_ACTIVE}, Integer.class);
        if (count <= 0) {
            throw new ReservationCredentialsException(MessageConstants.ERROR_MESSAGE_RESERVATION_NOT_FOUND);
        }
    }

    @Override
    public void isReservationExistWithGivenReservation(ReservationDTOForAdd dto) throws ReservationCredentialsException {
        int count = jdbcTemplate.queryForObject(IS_RESERVATION_EXIST_WITH_GIVEN_RESERVATION_SQL, new Object[] {dto.getStartTime().toString(), dto.getEndTime().toString(), dto.getStartTime().toString(), dto.getEndTime().toString(), dto.getStartTime().toString(), dto.getEndTime().toString(), dto.getDate().toString(), dto.getRoomId(), dto.getStatus()}, Integer.class);
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

//    @Override
//    public Reservation findReservationById(long id) {
//
//        Reservation reservation = jdbcTemplate.queryForObject(findReservationByIdSql, new Object[]{id}, new RowMapper<Reservation>() {
//            @Override
//            public Reservation mapRow(ResultSet rs, int i) throws SQLException {
//                Reservation reservation1 = new Reservation();
//                reservation1.setId(rs.getLong("reservation_id"));
//                reservation1.setFullname(rs.getString("fullname"));
//                reservation1.setTopic(rs.getString("topic"));
//                reservation1.setDate(rs.getDate("date").toLocalDate());
//                reservation1.setStartTime(rs.getTime("start_time").toLocalTime());
//                reservation1.setEndTime(rs.getTime("end_time").toLocalTime());
//
//                Room room = new Room();
//                room.setId(rs.getLong("room_id"));
//                room.setName(rs.getString("name"));
//
//                reservation1.setRoom(room);
//                return reservation1;
//            }
//        });
//
//        return reservation;
//    }
//
//    @Override
//    public List<ReservationDTO> findReservationsByEmployeeId(int employeeId, int fetchNext) {
//        List<ReservationDTO> reservationList = jdbcTemplate.query(findReservationsByEmployeeIdSql, new Object[]{employeeId, ReservationConstants.RESERVATION_STATUS_ACTIVE, ReservationConstants.RESERVATION_STATUS_UNCOMPLETED , ReservationConstants.RESERVATION_STATUS_FINISHED, ReservationConstants.RESERVATION_STATUS_COMPLETED, ReservationConstants.RESERVATION_OFFSET_NUMBER, fetchNext}, new ResultSetExtractor<List<ReservationDTO>>() {
//            @Override
//            public List<ReservationDTO> extractData(ResultSet rs) throws SQLException, DataAccessException {
//                List<ReservationDTO> list = new LinkedList<>();
//
//                while (rs.next()) {
//                    ReservationDTO reservation = new ReservationDTO();
//                    reservation.setId(rs.getLong("reservation_id"));
//                    reservation.setFullname(rs.getString("fullname"));
//                    reservation.setTopic(rs.getString("topic"));
//                    reservation.setDate(rs.getDate("date"));
//                    reservation.setStartTime(rs.getTime("start_time").toLocalTime());
//                    reservation.setEndTime(rs.getTime("end_time").toLocalTime());
//                    reservation.setRowNumber(rs.getInt("number"));
//
//                    Room room = new Room();
//                    room.setId(rs.getLong("room_id"));
//                    room.setName(rs.getString("name"));
//
//                    reservation.setRoom(room);
//                    list.add(reservation);
//                }
//                return list;
//            }
//        });
//
//        return reservationList;
//    }
//
//
//    @Override
//    public void updateReservation(Reservation reservation, boolean hasChanged) throws ReservationCredentialsException {
//        if (hasChanged) {
//            if (isReservationExist(reservation)) {
//                throw new ReservationCredentialsException(MessageConstants.ERROR_MESSAGE_RESERVATION_ALREADY_EXIST);
//            }
//        }
//
//        int affectedRows = jdbcTemplate.update(updateReservationSql, reservation.getFullname(), reservation.getTopic(), reservation.getDate(), reservation.getStartTime(), reservation.getEndTime(), reservation.getRoom().getId(), reservation.getId());
//        if (affectedRows == 0) {
//            throw new ReservationCredentialsException(MessageConstants.ERROR_MESSAGE_INTERNAL_ERROR);
//        }
//    }
//
//    @Override
//    public void deleteReservationById(long reservationId, int employeeId) throws ReservationCredentialsException {
//        int affectedRows = jdbcTemplate.update(deleteReservationByIdSql, ReservationConstants.RESERVATION_STATUS_DELETED, reservationId, employeeId);
//        if (affectedRows == 0) {
//            throw new ReservationCredentialsException(MessageConstants.ERROR_MESSAGE_INTERNAL_ERROR);
//        }
//    }
//
//    @Override
//    public List<ReservationDTO> findReservationsByStatusAndStatus2(int status, int status2, int fetchNext) {
//        List<ReservationDTO> reservationList = jdbcTemplate.query(findReservationsByStatusAndStatus2Sql, new Object[]{ReservationConstants.SEVEN_DAYS, status, status2, ReservationConstants.RESERVATION_OFFSET_NUMBER, fetchNext}, new ResultSetExtractor<List<ReservationDTO>>() {
//            @Override
//            public List<ReservationDTO> extractData(ResultSet rs) throws SQLException, DataAccessException {
//                List<ReservationDTO> list = new LinkedList<>();
//
//                while (rs.next()) {
//                    ReservationDTO reservation = new ReservationDTO();
//                    reservation.setId(rs.getLong("reservation_id"));
//                    reservation.setFullname(rs.getString("fullname"));
//                    reservation.setTopic(rs.getString("topic"));
//                    reservation.setDate(rs.getDate("date"));
//                    reservation.setStartTime(rs.getTime("start_time").toLocalTime());
//                    reservation.setEndTime(rs.getTime("end_time").toLocalTime());
//                    reservation.setRowNumber(rs.getInt("number"));
//
//                    Employee employee = new Employee();
//                    employee.setId(rs.getInt("employee_id"));
//
//                    Person person = new Person();
//                    person.setName(rs.getString("name"));
//                    person.setSurname(rs.getString("surname"));
//
//                    employee.setPerson(person);
//
//                    Room room = new Room();
//                    room.setId(rs.getLong("room_id"));
//                    room.setName(rs.getString("room_name"));
//
//                    reservation.setEmployee(employee);
//                    reservation.setRoom(room);
//                    list.add(reservation);
//                }
//                return list;
//            }
//        });
//
//        return reservationList;
//    }
//
//    @Override
//    public void updateReservationStatusAndStatus2(int status, int status2, int reservationId, int employeeId) throws ReservationCredentialsException {
//        int affectedRows = jdbcTemplate.update(updateReservationStatusAndStatus2Sql, status, status2, reservationId, employeeId);
//        if (affectedRows == 0) {
//            throw new ReservationCredentialsException(MessageConstants.ERROR_MESSAGE_INTERNAL_ERROR);
//        }
//    }
//
//    @Override
//    public void updateReservationStatus() {
//        jdbcTemplate.update(updateReservationStatusSql, ReservationConstants.RESERVATION_STATUS_FINISHED, ReservationConstants.RESERVATION_STATUS_COMPLETED, ReservationConstants.RESERVATION_STATUS_ACTIVE);
//    }

//    //private methods
//    private boolean isReservationExist(Reservation reservation) {
//        boolean result = false;
//        int count = jdbcTemplate.queryForObject(isReservationExistSql, new Object[] {reservation.getStartTime().toString(), reservation.getEndTime().toString(), reservation.getStartTime().toString(), reservation.getEndTime().toString(), reservation.getStartTime().toString(), reservation.getEndTime().toString(), reservation.getDate().toString(), reservation.getRoom().getId(), ReservationConstants.RESERVATION_STATUS_ACTIVE}, Integer.class);
//        if (count > 0) {
//            result = true;
//        }
//        return result;
//    }

}
