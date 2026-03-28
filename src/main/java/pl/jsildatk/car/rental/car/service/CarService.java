package pl.jsildatk.car.rental.car.service;

import pl.jsildatk.car.rental.ValidationException;
import pl.jsildatk.car.rental.car.domain.Car;
import pl.jsildatk.car.rental.car.domain.CarType;
import pl.jsildatk.car.rental.car.exception.CarDoesNotExistException;
import pl.jsildatk.car.rental.limits.exceptions.LimitsExceededException;

import java.math.BigDecimal;

/**
 * Service for car operations.
 */
public interface CarService {

    /**
     * Get car based on the id.
     *
     * @param carId id of the car
     * @return existing car with given id
     * @throws CarDoesNotExistException if car does not exist
     */
    Car getCar(String carId) throws CarDoesNotExistException;

    /**
     * Add car of given type and given price per day.
     *
     * @param carType     type of the car
     * @param pricePerDay price per day - must be positive
     * @return added car with generated id
     * @throws ValidationException     if price per day is not greater than zero
     * @throws LimitsExceededException if adding this car would exceed configured limits
     * @see pl.jsildatk.car.rental.limits.service.LimitsService#tryIncreaseObjectCountForType(Object, long)
     */
    Car addCar(CarType carType, BigDecimal pricePerDay) throws ValidationException, LimitsExceededException;

}
