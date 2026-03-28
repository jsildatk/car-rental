package pl.jsildatk.car.rental.limits.exceptions;

public class LimitsExceededException extends Exception {
    public LimitsExceededException(Class<?> clazz, long limit) {
        super(String.format("Adding object of type: %s will exceed limit (%d)", clazz.toString(), limit));
    }
}
