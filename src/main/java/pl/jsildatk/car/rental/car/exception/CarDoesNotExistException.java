package pl.jsildatk.car.rental.car.exception;

public class CarDoesNotExistException extends Exception {

    public CarDoesNotExistException(String id) {
        super(String.format("Car with id: %s does not exist", id));
    }
}
