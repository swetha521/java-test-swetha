package com.lbg.boardroom.bookings.service;

import com.lbg.boardroom.bookings.exception.BookingServiceException;
import com.lbg.boardroom.bookings.model.BookingRequest;
import com.lbg.boardroom.bookings.model.Meeting;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;

import static com.lbg.boardroom.bookings.service.BookingRequestParser.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BookingRequestParserTest {
    private final BookingRequestParser bookingRequestParser = new BookingRequestParser();

    @Test
    public void parse_ShouldReturnExpectedResult_WhenInputStringIsValid() {
        String inputText = "0900 1730\n" +
                "2020-01-18 10:17:06 EMP001\n" +
                "2020-01-21 09:00 2\n";
        BookingRequest expectedResult = new BookingRequest(LocalTime.parse("0900", TIME_FORMATTER), LocalTime.parse("1730", TIME_FORMATTER),
                Collections.singletonList(Meeting.builder()
                        .meetingDuration(2)
                        .requestDateTime(LocalDateTime.parse("2020-01-18 10:17:06", DATE_TIME_WITH_HH_MM_SS))
                        .employeeId("EMP001")
                        .startTime(LocalDateTime.parse("2020-01-21 09:00", DATE_TIME_WITH_ONLY_HH_MM))
                        .build()));

        BookingRequest bookingRequest = bookingRequestParser.parse(inputText);
        assertEquals(expectedResult.toString(), bookingRequest.toString());
    }

    @Test
    public void parse_ShouldThrowBookingServiceException_WhenInputStringIsIncomplete() {
        String inputText = "0900 1730\n";
        BookingServiceException exception = assertThrows(BookingServiceException.class, () -> bookingRequestParser.parse(inputText));
        assertEquals("Request payload has insufficient/invalid data for processing. Minimum number of lines expected is 3", exception.getMessage());
    }

    @Test
    public void parse_ShouldThrowBookingServiceException_WhenInputStringHasInvalidFormat() {
        String inputText = "0900 1730\n" +
                "2020-01-18 10:17 EMP001\n" +
                "2020-01-21 09:00 2\n";
        BookingServiceException exception = assertThrows(BookingServiceException.class, () -> bookingRequestParser.parse(inputText));
        assertEquals("Request has datetime/time value in invalid format [2020-01-18 10:17]. Text '2020-01-18 10:17' could not be parsed at index 16", exception.getMessage());
    }

    @Test
    public void parse_ShouldThrowBookingServiceException_WhenDurationValueIsNotSpecified() {
        String inputText = "0900 1730\n" +
                "2020-01-18 10:17:06 EMP001\n" +
                "2020-01-21 09:00\n";
        assertThrows(BookingServiceException.class, () -> bookingRequestParser.parse(inputText));
    }

    @Test
    public void parse_ShouldThrowBookingServiceException_WhenOfficeHoursIsNotSpecified() {
        String inputText = "0900\n" +
                "2020-01-18 10:17:06 EMP001\n" +
                "2020-01-21 09:00\n";
        BookingServiceException exception = assertThrows(BookingServiceException.class, () -> bookingRequestParser.parse(inputText));
        assertEquals("First line in request payload should contain office start and end hours, example: 0900 1730", exception.getMessage());
    }
}