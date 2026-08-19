package com.example.petnow.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;

class ReservationTest {

    private static final Pattern RESERVATION_NUMBER_PATTERN = Pattern.compile("PN-\\d{8}-[0-9A-F]{8}");

    @Test
    void createsReservationNumberWithStableFormat() {
        String reservationNumber = Reservation.createReservationNo();

        assertThat(reservationNumber).matches(RESERVATION_NUMBER_PATTERN);
    }
}
