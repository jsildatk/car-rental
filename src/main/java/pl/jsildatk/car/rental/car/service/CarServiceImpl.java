package pl.jsildatk.car.rental.car.service;

import pl.jsildatk.car.rental.ValidationException;
import pl.jsildatk.car.rental.car.domain.Car;
import pl.jsildatk.car.rental.car.domain.CarType;
import pl.jsildatk.car.rental.car.exception.CarDoesNotExistException;
import pl.jsildatk.car.rental.limits.exceptions.LimitsExceededException;
import pl.jsildatk.car.rental.limits.service.LimitsService;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class CarServiceImpl implements CarService {

    private final Map<String, Car> cars = new ConcurrentHashMap<>();

    private final LimitsService<CarType> limitsService;

    public CarServiceImpl(LimitsService<CarType> limitsService) {
        this.limitsService = limitsService;
    }

    @Override
    public Car getCar(String carId) throws CarDoesNotExistException {
        Car car = cars.get(carId);
        if (car == null) {
            throw new CarDoesNotExistException(carId);
        }

        return car;
    }

    @Override
    public Car addCar(CarType carType, BigDecimal pricePerDay) throws ValidationException, LimitsExceededException {
        if (pricePerDay.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException(String.format("Price per day must be greater than 0 (%s)", pricePerDay.toPlainString()));
        }

        limitsService.tryIncreaseObjectCountForType(carType, 1);
        Car car = new Car(UUID.randomUUID().toString(), carType, pricePerDay);
        cars.put(car.id(), car);
        return car;
    }
}
