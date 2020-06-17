package com.tracking.events.repository;

import com.tracking.events.repository.entity.Event;
import com.tracking.events.repository.entity.WeeklyReport;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * JPA Interface of the event weekly report. The @Query annotation has been used with nativeQuery set to true.
 *
 * @version 1.0
 * @dete 05-18-2020
 */
@Repository
public interface EventWeeklyReportRepository extends CrudRepository<Event, Long> {
    @Query(nativeQuery = true, value =
            "select avg(e.distance/NULLIF(e.duration,0)) as speed, avg(e.distance) as distance, extract(week from e.date) as week, extract(year from e.date) as year " +
                    " from event e, user u " +
                    " where e.user_id=u.id and u.id=:user_id " +
                    " group by extract(week from e.date),extract(year from e.date) order by extract(year from e.date) desc, extract(week from e.date) desc")
    List<WeeklyReport> findWeeklyReport(int user_id);
}