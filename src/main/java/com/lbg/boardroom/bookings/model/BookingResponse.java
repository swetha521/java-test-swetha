package com.lbg.boardroom.bookings.model;


import lombok.*;

import java.util.List;

@Getter
@EqualsAndHashCode
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class BookingResponse {
    private String date;
    private List<Booking> bookings;
}
