package com.lbg.boardroom.bookings.controller;

import com.lbg.boardroom.bookings.exception.BookingServiceException;
import com.lbg.boardroom.bookings.model.BookingResponse;
import com.lbg.boardroom.bookings.model.Error;
import com.lbg.boardroom.bookings.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.ConstraintViolationException;
import javax.validation.Valid;
import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.TEXT_PLAIN_VALUE;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class BookingController {
    private final BookingService bookingService;

    @PostMapping(value = "/bookings", consumes = TEXT_PLAIN_VALUE, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<List<BookingResponse>> bookBoardRoom(@Valid @RequestBody String bookingRequest) {
        return ResponseEntity.ok().body(bookingService.book(bookingRequest));
    }

    @ExceptionHandler(value = {ConstraintViolationException.class, BookingServiceException.class})
    public ResponseEntity<Error> handleInvalidRequestException(Exception exception) {
        return ResponseEntity.badRequest().body(new Error(exception.getMessage()));
    }
}