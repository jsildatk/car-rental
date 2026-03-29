package pl.jsildatk.car.rental.reservation.domain;

import java.math.BigDecimal;
import java.time.Instant;

public record Reservation(String id, String carId, String customerId, Instant startDate, Instant endDate, BigDecimal cost) {
}
