package pl.jsildatk.car.rental.reservation.service;

import pl.jsildatk.car.rental.ValidationException;
import pl.jsildatk.car.rental.car.domain.Car;
import pl.jsildatk.car.rental.car.exception.CarDoesNotExistException;
import pl.jsildatk.car.rental.reservation.domain.Reservation;
import pl.jsildatk.car.rental.reservation.exceptions.CarNotAvailableException;
import pl.jsildatk.car.rental.car.service.CarService;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ReservationServiceImpl implements ReservationService {

    private final Map<String, List<Reservation>> reservationsPerCar = new ConcurrentHashMap<>();

    private final CarService carService;
    private final Clock clock;

    public ReservationServiceImpl(Clock clock, CarService carService) {
        this.clock = clock;
        this.carService = carService;
    }

    @Override
    public Reservation makeReservation(String carId, String customerId, Instant startTime, int numberOfDays)
            throws ValidationException, CarNotAvailableException, CarDoesNotExistException {
        if (numberOfDays <= 0) {
            throw new ValidationException("Number of days must be greater than zero");
        } else if (clock.instant().isAfter(startTime)) {
            throw new ValidationException("Start time cannot be in the past");
        }

        Car car = carService.getCar(carId);
        Reservation reservation;

        synchronized (car) {
            Instant endTime = startTime.plus(numberOfDays, ChronoUnit.DAYS);
            if (!isCarAvailable(carId, startTime, endTime)) {
                throw new CarNotAvailableException(carId, startTime, endTime);
            }
            reservation = new Reservation(UUID.randomUUID().toString(), carId, customerId, startTime, endTime, calculateCost(numberOfDays, car.pricePerDay()));
            mergeReservations(carId, reservation);
        }

        return reservation;
    }

    private void mergeReservations(String carId, Reservation reservation) {
        List<Reservation> reservations = reservationsPerCar.get(carId);
        if (reservations != null) {
            reservations.add(reservation);
            reservationsPerCar.put(carId, reservations);
        } else {
            List<Reservation> newList = new ArrayList<>(Collections.singleton(reservation));
            reservationsPerCar.put(carId, Collections.synchronizedList(newList));
        }
    }

    private boolean isCarAvailable(String carId, Instant startTime, Instant endTime) {
        List<Reservation> reservationsForCar = reservationsPerCar.get(carId);
        return reservationsForCar == null || reservationsForCar.stream()
                .noneMatch(reservation -> isOverlapping(reservation, startTime, endTime));
    }

    private boolean isOverlapping(Reservation reservation, Instant startTime, Instant endTime) {
        return startTime.isBefore(reservation.endDate()) && endTime.isAfter(reservation.startDate());
    }

    private BigDecimal calculateCost(int numberOfDays, BigDecimal costPerDay) {
        return costPerDay.multiply(BigDecimal.valueOf(numberOfDays));
    }
}
