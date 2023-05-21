package com.lbg.boardroom.bookings.exception;

public class BookingServiceException extends RuntimeException {
    public BookingServiceException(String message) {
        super(message);
    }

    public BookingServiceException(String message, Exception exception) {
        super(message, exception);
    }
}
