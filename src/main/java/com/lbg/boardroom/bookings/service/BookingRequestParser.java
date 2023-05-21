package com.lbg.boardroom.bookings.service;

import com.lbg.boardroom.bookings.exception.BookingServiceException;
import com.lbg.boardroom.bookings.model.BookingRequest;
import com.lbg.boardroom.bookings.model.Meeting;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Parses and Validates text input to Booking request pojo
 */
@Slf4j
@Component
public class BookingRequestParser {

    public static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HHmm");
    public static final DateTimeFormatter DATE_TIME_WITH_HH_MM_SS = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    public static final DateTimeFormatter DATE_TIME_WITH_ONLY_HH_MM = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final String SPACE_SYMBOL = " ";

    /**
     * Parses input booking request string as per below format
     * [HHMM] [HHMM]
     * [YYYY-MM-DD HH:MM:SS] [string]
     * [YYYY-MM-DD HH:MM] [integer]
     * @param bookingRequestString
     * @return
     */
    public BookingRequest parse(@NotEmpty String bookingRequestString) {
        Object[] allInputLines = bookingRequestString.lines().toArray();
        if (allInputLines.length < 3) {
            throw new BookingServiceException("Request payload has insufficient/invalid data for processing. Minimum number of lines expected is 3");
        }
        String[] officeHours = allInputLines[0].toString().split(SPACE_SYMBOL);
        if (officeHours.length != 2) {
            throw new BookingServiceException("First line in request payload should contain office start and end hours, example: 0900 1730");
        }
        try {
            LocalTime bookingStartTime = LocalTime.parse(officeHours[0], TIME_FORMATTER);
            LocalTime bookingEndTime = LocalTime.parse(officeHours[1], TIME_FORMATTER);

            List<Meeting> meetings = new ArrayList<>();

            // Each meeting request data is present in 2 lines, so processing is batched for 2 lines to build each meeting request
            for (int i = 1; i < allInputLines.length; i = i + 2) {
                Meeting.MeetingBuilder meetingBuilder = Meeting.builder();

                // [Request submission time, in YYYY-MM-DD HH:MM:SS format] [Employee id]
                String[] bookingRequestLine1Words = allInputLines[i].toString().split(SPACE_SYMBOL);
                meetingBuilder.requestDateTime(LocalDateTime.parse(bookingRequestLine1Words[0] + SPACE_SYMBOL + bookingRequestLine1Words[1], DATE_TIME_WITH_HH_MM_SS));
                meetingBuilder.employeeId(bookingRequestLine1Words[2]);

                //[Meeting start time, in YYYY-MM-DD HH:MM format] [Meeting duration in hours]
                String[] bookingRequestLine2Words = allInputLines[i + 1].toString().split(SPACE_SYMBOL);
                meetingBuilder.startTime(LocalDateTime.parse(bookingRequestLine2Words[0] + SPACE_SYMBOL + bookingRequestLine2Words[1], DATE_TIME_WITH_ONLY_HH_MM));
                meetingBuilder.meetingDuration(Integer.parseInt(bookingRequestLine2Words[2]));

                meetings.add(meetingBuilder.build());
            }
            log.info("Parsing is successful {}", meetings);
            return new BookingRequest(bookingStartTime, bookingEndTime, meetings);
        } catch (DateTimeParseException exception) {
            log.error(exception.getMessage(), exception);
            throw new BookingServiceException("Request has datetime/time value in invalid format [" + exception.getParsedString() + "]. " + exception.getMessage());
        } catch (Exception exception) {
            log.error(exception.getMessage(), exception);
            throw new BookingServiceException("Request payload is invalid. " + exception.getMessage(), exception);
        }
    }
}
