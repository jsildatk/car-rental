package pl.jsildatk.car.rental.reservation.exceptions;

import java.time.LocalDateTime;

public class CarNotAvailableException extends Exception {

    public CarNotAvailableException(String id, LocalDateTime start, LocalDateTime end) {
        super(String.format("Car with id: %s is not available in requested period of time: %s - %s", id, start, end));
    }
}
