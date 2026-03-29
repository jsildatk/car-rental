package pl.jsildatk.car.rental.reservation.exceptions;

import java.time.Instant;

public class CarNotAvailableException extends Exception {

    public CarNotAvailableException(String id, Instant start, Instant end) {
        super(String.format("Car with id: %s is not available in requested period of time: %s - %s", id, start, end));
    }
}
