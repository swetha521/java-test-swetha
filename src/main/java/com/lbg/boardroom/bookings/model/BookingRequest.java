package com.lbg.boardroom.bookings.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalTime;
import java.util.List;

@AllArgsConstructor
@ToString
@Getter
public class BookingRequest {
    private final LocalTime officeStartTime;
    private final LocalTime officeEndTime;
    private final List<Meeting> meetings;
}
