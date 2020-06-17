package com.tracking.events.repository.entity;

/**
 * Default interface for WeeklyReport. Used by JPA in the EventWeeklyReportRepository class
 *
 * @version 1.0
 * @dete 05-18-2020
 */
public interface WeeklyReport {
    double getSpeed();

    double getDistance();

    int getWeek();

    int getYear();
}
