package io.pivotal.pal.tracker;


import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.util.CollectionUtils;

import javax.sql.DataSource;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class JdbcTimeEntryRepository implements TimeEntryRepository {

    private final static String INSERT_SQL = "INSERT INTO time_entries (project_id, user_id, date, hours) " +
            "VALUES (?, ?, ?, ?)";
    private final static String UPDATE_SQL = "UPDATE time_entries SET project_id = ?, user_id = ?, date = ?, hours = ? " +
            "WHERE id = ?";
    private final static String SELECT_SQL = "Select * from time_entries where id = ?";
    private final static String SELECT_ALL_SQL = "Select * from time_entries";
    private final static String DELETE_SQL = "DELETE from time_entries where id = ?";

    private JdbcTemplate jdbcTemplate;

    public JdbcTimeEntryRepository(DataSource dataSource) {
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public TimeEntry create(TimeEntry timeEntry) {

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(
                connection -> {
                    PreparedStatement ps =
                            connection.prepareStatement(INSERT_SQL, new String[] {"id"});
                    ps.setLong(1, timeEntry.getProjectId());
                    ps.setLong(2, timeEntry.getUserId());
                    ps.setDate(3, Date.valueOf(timeEntry.getDate()));
                    ps.setInt(4, timeEntry.getHours());
                    return ps;
                },
                keyHolder);

        timeEntry.setId(keyHolder.getKey().longValue());

        return timeEntry;
    }

    @Override
    public TimeEntry find(Long id) {
        List<Map<String, Object>> entries = jdbcTemplate.queryForList(SELECT_SQL, id);
        if (CollectionUtils.isEmpty(entries)) {
            return null;
        }
        return toTimeEntry(entries.get(0));
    }

    private TimeEntry toTimeEntry(Map<String, Object> entry) {
        return new TimeEntry(
                (Long) entry.get("id"),
                (Long) entry.get("project_id"),
                (Long) entry.get("user_id"),
                ((Date)entry.get("date")).toLocalDate(),
                (Integer) entry.get("hours")
        );
    }

    @Override
    public List<TimeEntry> list() {
        List<Map<String, Object>> entries = jdbcTemplate.queryForList(SELECT_ALL_SQL);
        if (CollectionUtils.isEmpty(entries)) {
            return Collections.emptyList();
        }
        return entries.stream().map(e -> toTimeEntry(e)).collect(Collectors.toList());
    }

    @Override
    public TimeEntry update(Long id, TimeEntry timeEntry) {

        long updated = jdbcTemplate.update(
                connection -> {
                    PreparedStatement ps =
                            connection.prepareStatement(UPDATE_SQL, new String[] {"id"});
                    ps.setLong(1, timeEntry.getProjectId());
                    ps.setLong(2, timeEntry.getUserId());
                    ps.setDate(3, Date.valueOf(timeEntry.getDate()));
                    ps.setInt(4, timeEntry.getHours());
                    ps.setLong(5, id);
                    return ps;
                });

        if (updated == 0) {
            return null;
        }

        timeEntry.setId(id);
        return timeEntry;
    }

    @Override
    public void delete(Long id) {
        jdbcTemplate.update(DELETE_SQL, id);
    }
}
