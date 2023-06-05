package com.lbg.boardroom.bookings.service;

import com.lbg.boardroom.bookings.model.BookingRequest;
import com.lbg.boardroom.bookings.model.Meeting;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static com.lbg.boardroom.bookings.service.BookingRequestParser.DATE_TIME_WITH_HH_MM_SS;
import static com.lbg.boardroom.bookings.service.BookingRequestParser.DATE_TIME_WITH_ONLY_HH_MM;
import static com.lbg.boardroom.bookings.service.BookingService.DATE_FORMATTER;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class BookingProcessorTest {
    private BookingProcessor bookingProcessor = new BookingProcessor();
    private BookingRequestParser bookingRequestParser = new BookingRequestParser();

    @Test
    public void processBookingRequest_ShouldReturnOnly1Meeting_WhenAllAreOverlappingRequests() {
        String inputText = "0900 1730\n" +
                "2024-01-18 12:34:56 EMP002\n" +
                "2024-01-21 09:00 2\n" +
                "2024-01-18 10:17:06 EMP001\n" +
                "2024-01-21 09:00 1\n" +
                "2024-01-19 09:28:23 EMP003\n" +
                "2024-01-21 09:00 2\n";

        BookingRequest bookingRequest = bookingRequestParser.parse(inputText);
        Map<LocalDate, List<Meeting>> result = bookingProcessor.processBookingRequest(bookingRequest);
        assertEquals(1, result.size());

        List<Meeting> meetings = result.get(LocalDate.parse("2024-01-21", DATE_FORMATTER));
        assertEquals(1, meetings.size());
        assertEquals(Meeting.builder()
                .meetingDuration(1)
                .requestDateTime(LocalDateTime.parse("2024-01-18 10:17:06", DATE_TIME_WITH_HH_MM_SS))
                .employeeId("EMP001")
                .startTime(LocalDateTime.parse("2024-01-21 09:00", DATE_TIME_WITH_ONLY_HH_MM))
                .build(), meetings.get(0));
    }
    @Test
    public void processBookingRequest_ShouldReturnMeetingInRequestDateTime_WhenOverlappingRequestsBookingTimes() {
        String inputText = "0800 1730\n" +
                "2024-01-18 12:34:56 EMP002\n" +
                "2024-01-21 09:00 2\n" +
                "2024-01-18 10:17:06 EMP001\n" +
                "2024-01-21 08:00 1\n" +
                "2024-01-19 09:28:23 EMP003\n" +
                "2024-01-21 09:00 1\n";

        BookingRequest bookingRequest = bookingRequestParser.parse(inputText);
        Map<LocalDate, List<Meeting>> result = bookingProcessor.processBookingRequest(bookingRequest);
        assertEquals(1, result.size());

        List<Meeting> meetings = result.get(LocalDate.parse("2024-01-21", DATE_FORMATTER));
        assertEquals(2, meetings.size());
        assertEquals(Meeting.builder()
                .meetingDuration(1)
                .requestDateTime(LocalDateTime.parse("2024-01-18 10:17:06", DATE_TIME_WITH_HH_MM_SS))
                .employeeId("EMP001")
                .startTime(LocalDateTime.parse("2024-01-21 08:00", DATE_TIME_WITH_ONLY_HH_MM))
                .build(), meetings.get(0));

        assertEquals("EMP002", meetings.get(1).getEmployeeId());
    }

    @Test
    public void processBookingRequest_ShouldReturnMeetingInRequestDateTime_WhenOverlappingRequestsBookingTimes_1() {
        String inputText = "0800 1730\n" +
                "2024-01-18 12:34:56 EMP002\n" +
                "2024-01-21 09:00 2\n" +
                "2024-01-18 10:17:06 EMP001\n" +
                "2024-01-21 08:00 1\n" +
                "2024-01-21 09:28:23 EMP003\n" +
                "2024-01-22 09:00 1\n" +
                "2024-01-20 09:28:23 EMP004\n" +
                "2024-01-22 09:00 1\n";

        BookingRequest bookingRequest = bookingRequestParser.parse(inputText);
        Map<LocalDate, List<Meeting>> result = bookingProcessor.processBookingRequest(bookingRequest);
        assertEquals(2, result.size());

        List<Meeting> meetings = result.get(LocalDate.parse("2024-01-21", DATE_FORMATTER));
        List<Meeting> secondDayMeetings = result.get(LocalDate.parse("2024-01-22", DATE_FORMATTER));

        assertEquals(2, meetings.size());
        assertEquals(1, secondDayMeetings.size());

        assertEquals(Meeting.builder()
                .meetingDuration(1)
                .requestDateTime(LocalDateTime.parse("2024-01-18 10:17:06", DATE_TIME_WITH_HH_MM_SS))
                .employeeId("EMP001")
                .startTime(LocalDateTime.parse("2024-01-21 08:00", DATE_TIME_WITH_ONLY_HH_MM))
                .build(), meetings.get(0));

        assertEquals("EMP002", meetings.get(1).getEmployeeId());
        assertEquals("EMP004", secondDayMeetings.get(0).getEmployeeId());
    }
    @Test
    public void processBookingRequest_ShouldNotReturnAnyRequests_WhenOutsideOfficeHours() {
        String inputText = "0900 1730\n" +
                "2024-01-18 12:34:56 EMP002\n" +
                "2024-01-21 17:00 1\n" +
                "2024-01-18 10:17:06 EMP001\n" +
                "2024-01-21 18:00 1\n" +
                "2024-01-19 09:28:23 EMP003\n" +
                "2024-01-21 08:00 2\n";

        BookingRequest bookingRequest = bookingRequestParser.parse(inputText);
        Map<LocalDate, List<Meeting>> processedBookings = bookingProcessor.processBookingRequest(bookingRequest);
        assertNotNull(processedBookings);
        assertTrue(processedBookings.isEmpty());
    }
}