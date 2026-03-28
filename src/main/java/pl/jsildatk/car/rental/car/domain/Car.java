package pl.jsildatk.car.rental.car.domain;

import java.math.BigDecimal;

public record Car(String id, CarType type, BigDecimal pricePerDay) {
}
