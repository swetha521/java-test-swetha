package com.lbg.boardroom.bookings.service;

import com.lbg.boardroom.bookings.exception.BookingServiceException;
import com.lbg.boardroom.bookings.model.BookingRequest;
import com.lbg.boardroom.bookings.model.BookingResponse;
import com.lbg.boardroom.bookings.model.Meeting;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static com.lbg.boardroom.bookings.service.BookingRequestParser.DATE_TIME_WITH_HH_MM_SS;
import static com.lbg.boardroom.bookings.service.BookingRequestParser.DATE_TIME_WITH_ONLY_HH_MM;
import static com.lbg.boardroom.bookings.service.BookingService.DATE_FORMATTER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {
    @Mock
    private BookingProcessor bookingProcessor;
    @Mock
    private BookingRequestParser bookingRequestParser;

    private BookingService bookingService;

    @BeforeEach
    void init() {
        bookingService = new BookingService(bookingRequestParser, bookingProcessor);
    }

    @Test
    public void book_ShouldReturnBookings_InOrderOfMeetingStartTime() {
        String inputText = "0900 1730\n" +
                "2024-01-18 12:34:56 EMP002\n" +
                "2024-01-21 09:00 2\n" +
                "2024-01-18 10:17:06 EMP001\n" +
                "2024-01-21 09:00 1\n" +
                "2024-01-19 09:28:23 EMP003\n" +
                "2024-01-21 09:00 2\n";

        Meeting meeting1 = Meeting.builder()
                .meetingDuration(2)
                .requestDateTime(LocalDateTime.parse("2020-01-18 10:17:06", DATE_TIME_WITH_HH_MM_SS))
                .employeeId("EMP001")
                .startTime(LocalDateTime.parse("2020-01-21 15:00", DATE_TIME_WITH_ONLY_HH_MM))
                .build();
        Meeting meeting2 = Meeting.builder()
                .meetingDuration(2)
                .requestDateTime(LocalDateTime.parse("2020-01-18 10:17:06", DATE_TIME_WITH_HH_MM_SS))
                .employeeId("EMP002")
                .startTime(LocalDateTime.parse("2020-01-21 13:00", DATE_TIME_WITH_ONLY_HH_MM))
                .build();
        Meeting meeting3 = Meeting.builder()
                .meetingDuration(2)
                .requestDateTime(LocalDateTime.parse("2020-01-18 10:17:06", DATE_TIME_WITH_HH_MM_SS))
                .employeeId("EMP003")
                .startTime(LocalDateTime.parse("2020-01-21 10:00", DATE_TIME_WITH_ONLY_HH_MM))
                .build();

        BookingRequest bookingRequest = mock(BookingRequest.class);
        when(bookingRequestParser.parse(inputText)).thenReturn(bookingRequest);
        when(bookingProcessor.processBookingRequest(bookingRequest)).thenReturn(Map.of(
                LocalDate.parse("2024-01-21", DATE_FORMATTER), List.of(meeting1, meeting2, meeting3)));

        List<BookingResponse> bookingResponses = bookingService.book(inputText);

        assertEquals("EMP003", bookingResponses.get(0).getBookings().get(0).getEmployeeId());
        assertEquals("EMP002", bookingResponses.get(0).getBookings().get(1).getEmployeeId());
        assertEquals("EMP001", bookingResponses.get(0).getBookings().get(2).getEmployeeId());

        verify(bookingRequestParser, times(1)).parse(any());
        verify(bookingProcessor, times(1)).processBookingRequest(any());
    }

    @Test
    public void book_ShouldNotProcessBookings_WhenRequestPayloadIsInvalid() {
        String inputText = "0900 1730\n";
        when(bookingRequestParser.parse(inputText)).thenThrow(new BookingServiceException("Invalid Request payload"));

        assertThrows(BookingServiceException.class, () -> bookingService.book(inputText));
        verify(bookingRequestParser, times(1)).parse(any());
        verifyNoInteractions(bookingProcessor);
    }
}