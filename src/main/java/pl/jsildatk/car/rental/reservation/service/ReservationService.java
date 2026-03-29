package pl.jsildatk.car.rental.reservation.service;

import pl.jsildatk.car.rental.ValidationException;
import pl.jsildatk.car.rental.car.exception.CarDoesNotExistException;
import pl.jsildatk.car.rental.reservation.domain.Reservation;
import pl.jsildatk.car.rental.reservation.exceptions.CarNotAvailableException;

import java.time.Instant;

/**
 * Service for handling reservations.
 */
public interface ReservationService {

    /**
     * Make a reservation for given car and customer for given number of days.
     * Date time together with total cost is calculated and stored.
     * This method is thread-safe and guarantees that there will be no overlapping reservations made.
     *
     * @param carId        car to reserver
     * @param customerId   customer that makes reservation
     * @param startTime    starting time of reservation - date and time
     * @param numberOfDays - number of days to make reservation for
     * @return created and saved {@link Reservation} object
     * @throws ValidationException      if numberOfDays is equal or less than 0 or startTime is in the past
     * @throws CarDoesNotExistException if requested car does not exist
     * @throws CarNotAvailableException if requested car isn't available in given time period
     */
    Reservation makeReservation(String carId, String customerId, Instant startTime, int numberOfDays)
            throws ValidationException, CarDoesNotExistException, CarNotAvailableException;

}
