package pl.jsildatk.car.rental.car.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.jsildatk.car.rental.ValidationException;
import pl.jsildatk.car.rental.car.domain.Car;
import pl.jsildatk.car.rental.car.domain.CarType;
import pl.jsildatk.car.rental.limits.exceptions.LimitsExceededException;
import pl.jsildatk.car.rental.limits.service.LimitsService;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CarServiceTest {

    @Mock
    private LimitsService<CarType> limitsService;

    private CarService carService;

    @BeforeEach
    void setUp() {
        carService = new CarServiceImpl(limitsService);
    }

    @Test
    void exceptionIsThrownWhenGettingCarAndCarDoesNotExist() {
        // given
        String notExistingId = "nonExisitng";

        // when & then
        assertThatThrownBy(() -> carService.getCar(notExistingId))
                .hasMessageContaining(notExistingId);
    }

    @Test
    void existingCarIsReturned() throws Exception {
        // given
        Car addedCar = carService.addCar(CarType.SEDAN, BigDecimal.valueOf(100));

        // when
        Car result = carService.getCar(addedCar.id());

        // then
        assertThat(result).isEqualTo(addedCar);
    }

    @Test
    void exceptionIsThrownWhenPricePerDayIsNegative() {
        // given
        BigDecimal pricePerDay = BigDecimal.valueOf(-213);

        // when & then
        assertThatThrownBy(() -> carService.addCar(CarType.SEDAN, pricePerDay))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void exceptionIsThrownWhenPricePerDayIsZero() {
        assertThatThrownBy(() -> carService.addCar(CarType.SEDAN, BigDecimal.ZERO))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void carIsAddedWhenLimitsWillNotBeExceeded() throws Exception {
        // when
        CarType carType = CarType.SEDAN;
        Car result = carService.addCar(carType, BigDecimal.valueOf(100));

        // then
        assertThat(result).isNotNull();
        assertThat(result.id()).isNotNull();
        verify(limitsService).tryIncreaseObjectCountForType(carType, 1);
    }

    @Test
    void exceptionIsThrownWhenLimitsWillBeExceeded() throws Exception {
        // given
        CarType carType = CarType.SEDAN;
        doThrow(new LimitsExceededException(CarType.class, 1)).when(limitsService).tryIncreaseObjectCountForType(carType, 1);

        // when
        assertThatThrownBy(() -> carService.addCar(carType, BigDecimal.valueOf(100)))
                .isInstanceOf(LimitsExceededException.class);
    }

}