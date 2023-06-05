package com.lbg.boardroom.bookings.service;

import com.lbg.boardroom.bookings.model.BookingRequest;
import com.lbg.boardroom.bookings.model.Meeting;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
        Set<Meeting> meetings = findNonOverlappingMeetings(bookingRequest);
        return meetings.stream()
                .collect(Collectors.groupingBy(meeting -> meeting.getStartTime().toLocalDate(), LinkedHashMap::new, Collectors.toList()));
    }

    private Set<Meeting> findNonOverlappingMeetings(BookingRequest bookingRequest) {
        TreeSet<Meeting> meetingsWithoutOverlap = new TreeSet<>(Comparator.comparing(Meeting::getStartTime));
        Meeting previousMeeting = null;
        LocalTime officeStartTime = bookingRequest.getOfficeStartTime();
        LocalTime officeEndTime = bookingRequest.getOfficeEndTime();
        bookingRequest.getMeetings().sort(Comparator.comparing(Meeting::getStartTime));
        for (Meeting meeting : bookingRequest.getMeetings()) {
            if (isMeetingWithinOfficeHours(officeStartTime, officeEndTime, meeting)) {
                if (previousMeeting != null && meeting.getStartTime().isBefore(previousMeeting.getMeetingEndTime())) {
                    if (meeting.getRequestDateTime().isBefore(previousMeeting.getRequestDateTime())) {
                        meetingsWithoutOverlap.remove(previousMeeting);
                        meetingsWithoutOverlap.add(meeting);
                        previousMeeting = meeting;
                    }
                } else {
                    meetingsWithoutOverlap.add(meeting);
                    previousMeeting = meeting;
                }
            }
        }
        return meetingsWithoutOverlap;
    }

    private boolean isMeetingWithinOfficeHours(LocalTime officeStartTime, LocalTime officeEndTime, Meeting meeting) {
        LocalDateTime startDateTime = meeting.getStartTime();
        LocalDateTime endDateTime = meeting.getMeetingEndTime();
        return !startDateTime.toLocalTime().isBefore(officeStartTime) && !endDateTime.toLocalTime().isAfter(officeEndTime);
    }
}
