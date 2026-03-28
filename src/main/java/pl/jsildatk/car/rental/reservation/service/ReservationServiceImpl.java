package pl.jsildatk.car.rental.reservation.service;

import pl.jsildatk.car.rental.ValidationException;
import pl.jsildatk.car.rental.car.domain.Car;
import pl.jsildatk.car.rental.car.exception.CarDoesNotExistException;
import pl.jsildatk.car.rental.reservation.domain.Reservation;
import pl.jsildatk.car.rental.reservation.exceptions.CarNotAvailableException;
import pl.jsildatk.car.rental.car.service.CarService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ReservationServiceImpl implements ReservationService {

    private final Map<String, Reservation> reservations = new ConcurrentHashMap<>();

    private final CarService carService;

    public ReservationServiceImpl(CarService carService) {
        this.carService = carService;
    }

    @Override
    public Reservation makeReservation(String carId, String customerId, LocalDateTime startTime, int numberOfDays)
            throws ValidationException, CarNotAvailableException, CarDoesNotExistException {
        if (numberOfDays <= 0) {
            throw new ValidationException("Number of days must be greater than zero");
        }

        Car car = carService.getCar(carId);
        Reservation reservation;

        synchronized (car) {
            LocalDateTime endTime = startTime.plusDays(numberOfDays);
            if (!isCarAvailable(carId, startTime, endTime)) {
                throw new CarNotAvailableException(carId, startTime, endTime);
            }
            reservation = new Reservation(UUID.randomUUID().toString(), carId, customerId, startTime, endTime, calculateCost(numberOfDays, car.pricePerDay()));
            reservations.put(reservation.id(), reservation);
        }

        return reservation;
    }

    private boolean isCarAvailable(String carId, LocalDateTime startTime, LocalDateTime endTime) {
        List<Reservation> reservationsForCar = findReservationsForCar(carId);

        return reservationsForCar.isEmpty() || reservationsForCar.stream()
                .noneMatch(reservation -> isOverlapping(reservation, startTime, endTime));
    }

    private List<Reservation> findReservationsForCar(String carId) {
        return reservations.values().stream()
                .filter(reservation -> reservation.carId().equals(carId))
                .toList();
    }

    private boolean isOverlapping(Reservation reservation, LocalDateTime startTime, LocalDateTime endTime) {
        return startTime.isBefore(reservation.endDate()) && endTime.isAfter(reservation.startDate());
    }

    private BigDecimal calculateCost(int numberOfDays, BigDecimal costPerDay) {
        return costPerDay.multiply(BigDecimal.valueOf(numberOfDays));
    }
}
