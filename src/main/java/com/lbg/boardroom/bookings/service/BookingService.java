package com.lbg.boardroom.bookings.service;

import com.lbg.boardroom.bookings.model.Booking;
import com.lbg.boardroom.bookings.model.BookingRequest;
import com.lbg.boardroom.bookings.model.BookingResponse;
import com.lbg.boardroom.bookings.model.Meeting;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotEmpty;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Slf4j
@Component
public class BookingService {
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private final BookingRequestParser bookingRequestParser;
    private final BookingProcessor bookingProcessor;

    /**
     * Parses booking request string to POJO and process the booking request and builds response
     * @param bookingRequest
     * @return
     */
    public List<BookingResponse> book(@NotEmpty String bookingRequest) {
        log.info("Received booking request : [{}]", bookingRequest);
        final BookingRequest bookingRequestData = bookingRequestParser.parse(bookingRequest);
        Map<LocalDate, List<Meeting>> meetingsMap = bookingProcessor.processBookingRequest(bookingRequestData);
        List<BookingResponse> bookingResponses = buildBookingResponse(meetingsMap);
        log.info("Booking response {}", bookingResponses);
        return bookingResponses;
    }

    private List<BookingResponse> buildBookingResponse(Map<LocalDate, List<Meeting>> bookingsMap) {
        List<BookingResponse> bookingResponseData = new ArrayList<>();
        bookingsMap.forEach((date, bookings) -> {
            List<Booking> confirmedBookings = bookings.stream().sorted(Comparator.comparing(Meeting::getStartTime)).map(booking -> new Booking(booking.getEmployeeId(), booking.getStartTime().toLocalTime().format(TIME_FORMATTER), booking.getMeetingEndTime().toLocalTime().format(TIME_FORMATTER))).collect(Collectors.toList());
            bookingResponseData.add(new BookingResponse(date.format(DATE_FORMATTER), confirmedBookings));
        });
        return bookingResponseData;
    }
}
