package pl.jsildatk.car.rental.reservation.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record Reservation(String id, String carId, String customerId, LocalDateTime startDate, LocalDateTime endDate, BigDecimal cost) {
}
