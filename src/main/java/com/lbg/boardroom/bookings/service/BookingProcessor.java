package com.lbg.boardroom.bookings.service;

import com.lbg.boardroom.bookings.model.BookingRequest;
import com.lbg.boardroom.bookings.model.Meeting;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Component responsible to process bookings by applying booking rules
 * - No part of a meeting may fall outside of office hours.
 * - Meetings may not overlap.
 * - The booking submission times are guaranteed to be unique.
 */
@Component
public class BookingProcessor {

    public Map<LocalDate, List<Meeting>> processBookingRequest(final BookingRequest bookingRequest) {
        bookingRequest.getMeetings().sort(Comparator.comparing(Meeting::getStartTime));
        Set<Meeting> meetings = findNonOverlappingMeetings(bookingRequest);
        return meetings.stream().collect(Collectors.groupingBy(meeting -> meeting.getStartTime().toLocalDate(), TreeMap::new, Collectors.toList()));
    }

    private Set<Meeting> findNonOverlappingMeetings(BookingRequest bookingRequest) {
        Set<Meeting> meetingsWithoutOverlap = new HashSet<>();
        Meeting previousMeeting = null;
        for (Meeting meeting : bookingRequest.getMeetings()) {
            if (isMeetingWithinOfficeHours(bookingRequest.getOfficeStartTime(), bookingRequest.getOfficeEndTime(), meeting)) {
                if (previousMeeting != null && (meeting.getStartTime().isBefore(previousMeeting.getMeetingEndTime()))) {

                    if (meeting.getRequestDateTime().isBefore(previousMeeting.getRequestDateTime())) {
                        meetingsWithoutOverlap.remove(previousMeeting);
                        meetingsWithoutOverlap.add(meeting);
                    }
                } else {
                    meetingsWithoutOverlap.add(meeting);
                }
                previousMeeting = meeting;
            }
        }
        return meetingsWithoutOverlap;
    }

    private boolean isMeetingWithinOfficeHours(LocalTime officeStartTime, LocalTime officeEndTime, Meeting meeting) {
        return !meeting.getStartTime().toLocalTime().isBefore(officeStartTime) && !meeting.getMeetingEndTime().toLocalTime().isAfter(officeEndTime);
    }
}
