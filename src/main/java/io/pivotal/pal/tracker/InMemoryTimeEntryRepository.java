package io.pivotal.pal.tracker;

import java.sql.Time;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class InMemoryTimeEntryRepository implements  TimeEntryRepository {

    private AtomicLong idGenerator = new AtomicLong();
    Map<Long, TimeEntry> backingMap = new ConcurrentHashMap<>();

    @Override
    public TimeEntry create(TimeEntry timeEntry) {
        timeEntry.setId(idGenerator.incrementAndGet());
        backingMap.put(timeEntry.getId(), timeEntry);
        return timeEntry;
    }

    @Override
    public TimeEntry find(Long id) {
        return backingMap.get(id);
    }

    @Override
    public List<TimeEntry> list() {
        return new ArrayList<>(backingMap.values());
    }

    @Override
    public TimeEntry update(Long id, TimeEntry timeEntry) {
        TimeEntry existingTimeEntry = backingMap.get(id);
        if (existingTimeEntry != null) {
            timeEntry.setId(id);
            backingMap.put(id, timeEntry);
            return timeEntry;
        }
        return null;
    }

    @Override
    public void delete(Long id) {
        backingMap.remove(id);
    }
}
