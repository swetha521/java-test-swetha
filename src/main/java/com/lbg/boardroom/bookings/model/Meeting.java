package com.lbg.boardroom.bookings.model;

import lombok.*;

import java.time.LocalDateTime;

@Builder
@Getter
@ToString
@EqualsAndHashCode
public class Meeting {
    private LocalDateTime requestDateTime;
    private String employeeId;
    private LocalDateTime startTime;
    private int meetingDuration;

    public final LocalDateTime getMeetingEndTime() {
        return startTime.plusHours(meetingDuration);
    }
}
