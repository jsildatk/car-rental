package pl.jsildatk.car.rental.reservation.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.jsildatk.car.rental.Pair;
import pl.jsildatk.car.rental.ValidationException;
import pl.jsildatk.car.rental.car.domain.Car;
import pl.jsildatk.car.rental.car.domain.CarType;
import pl.jsildatk.car.rental.car.exception.CarDoesNotExistException;
import pl.jsildatk.car.rental.car.service.CarService;
import pl.jsildatk.car.rental.reservation.domain.Reservation;
import pl.jsildatk.car.rental.reservation.exceptions.CarNotAvailableException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock
    private CarService carService;

    private ReservationService reservationService;

    @BeforeEach
    void setUp() {
        reservationService = new ReservationServiceImpl(carService);
    }

    @Test
    void exceptionIsThrownWhenMakingReservationForNegativeNumberOfDays() {
        assertThatThrownBy(() -> reservationService.makeReservation("id", "1", LocalDateTime.now(), -3))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void exceptionIsThrownWhenMakingReservationForZeroDays() {
        assertThatThrownBy(() -> reservationService.makeReservation("id", "1", LocalDateTime.now(), 0))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void exceptionIsThrownWhenCarDoesNotExist() throws Exception {
        // given
        String carId = "id";
        when(carService.getCar(carId)).thenThrow(new CarDoesNotExistException(carId));

        // when & then
        assertThatThrownBy(() -> reservationService.makeReservation(carId, "1", LocalDateTime.now(), 5))
                .isInstanceOf(CarDoesNotExistException.class)
                .hasMessage(String.format("Car with id: %s does not exist", carId));
    }

    @Test
    void reservationIsMade() throws Exception {
        // given
        String carId = "id";
        when(carService.getCar(carId)).thenReturn(new Car(carId, CarType.SEDAN, BigDecimal.valueOf(1)));

        String customerId = "1";
        int numberOfDays = 2;
        LocalDateTime startTime = LocalDateTime.of(2026, 2, 3, 10, 0);

        // when
        Reservation reservation = reservationService.makeReservation(carId, customerId, startTime, numberOfDays);

        // then
        assertThat(reservation).isNotNull();
        assertThat(reservation.cost()).isEqualTo(BigDecimal.valueOf(2));
        assertThat(reservation.startDate()).isEqualTo(startTime);
        assertThat(reservation.endDate()).isEqualTo(startTime.plusDays(numberOfDays));
    }

    @Test
    void costIsCalculatedProperly() throws Exception {
        // given
        String carId = "id";
        BigDecimal costPerDay = BigDecimal.valueOf(15000.25);
        when(carService.getCar(carId)).thenReturn(new Car(carId, CarType.SEDAN, costPerDay));

        String customerId = "1";
        int numberOfDays = 40;
        LocalDateTime startTime = LocalDateTime.of(2026, 2, 3, 10, 0);

        // when
        Reservation reservation = reservationService.makeReservation(carId, customerId, startTime, numberOfDays);

        // then
        BigDecimal expectedCost = costPerDay.multiply(BigDecimal.valueOf(numberOfDays));
        assertThat(reservation.cost()).isEqualTo(expectedCost);
    }

    @Test
    void exceptionIsThrownWhenCarIsNotAvailableInGivenPeriod() throws Exception {
        // given
        String carId = "id";
        when(carService.getCar(carId)).thenReturn(new Car(carId, CarType.SEDAN, BigDecimal.valueOf(1)));

        String customerId = "1";
        LocalDateTime startTime = LocalDateTime.of(2026, 2, 3, 10, 0);

        Reservation reservation = reservationService.makeReservation(carId, customerId, startTime, 2);
        assertThat(reservation).isNotNull();

        // when & then
        assertThatThrownBy(() -> reservationService.makeReservation(carId, customerId, startTime, 2))
                .isInstanceOf(CarNotAvailableException.class)
                .hasMessageContaining("not available in requested period of time");
    }

    @Test
    void exceptionIsThrownWhenCarIsNotAvailableInGivenPeriodIfStartDateIsOk() throws Exception {
        // given
        String carId = "id";
        when(carService.getCar(carId)).thenReturn(new Car(carId, CarType.SEDAN, BigDecimal.valueOf(1)));

        String customerId = "1";
        LocalDateTime startTime = LocalDateTime.of(2026, 2, 3, 10, 0);

        Reservation reservation = reservationService.makeReservation(carId, customerId, startTime, 5);
        assertThat(reservation).isNotNull();

        // when & then
        assertThatThrownBy(() -> reservationService.makeReservation(carId, customerId, startTime.minusDays(2), 4))
                .isInstanceOf(CarNotAvailableException.class)
                .hasMessageContaining("not available in requested period of time");
    }

    @Test
    void reservationIsMadeIfItIsNotOverlappingTheExistingReservations() throws Exception {
        // given
        String carId = "id";
        when(carService.getCar(carId)).thenReturn(new Car(carId, CarType.SEDAN, BigDecimal.valueOf(1)));
        String customerId = "1";

        // 2026/02/03 10:00 - 2026/02/08 10:00
        LocalDateTime startTime1 = LocalDateTime.of(2026, 2, 3, 10, 0);
        Reservation reservation1 = reservationService.makeReservation(carId, customerId, startTime1, 5);
        assertThat(reservation1).isNotNull();

        // 2026/02/08 11:00 - 2026/02/10 11:00
        LocalDateTime startTime2 = LocalDateTime.of(2026, 2, 8, 11, 0);
        Reservation reservation2 = reservationService.makeReservation(carId, customerId, startTime2, 2);
        assertThat(reservation2).isNotNull();

        // 2026/02/12 08:00 - 2026/02/27 08:00
        LocalDateTime startTime3 = LocalDateTime.of(2026, 2, 12, 8, 0);

        // when
        Reservation result = reservationService.makeReservation(carId, customerId, startTime3, 15);

        // then
        assertThat(result).isNotNull();
    }

    @Test
    void differentCarsCanBeReservedInTheSameTimePeriod() throws Exception {
        // given
        String carId1 = "id1";
        String carId2 = "id2";
        String carId3 = "id3";
        when(carService.getCar(carId1)).thenReturn(new Car(carId1, CarType.SEDAN, BigDecimal.valueOf(1)));
        when(carService.getCar(carId2)).thenReturn(new Car(carId2, CarType.SUV, BigDecimal.valueOf(1)));
        when(carService.getCar(carId3)).thenReturn(new Car(carId3, CarType.VAN, BigDecimal.valueOf(1)));
        String customerId = "1";

        LocalDateTime startTime = LocalDateTime.of(2026, 2, 3, 10, 0);

        // when
        Reservation reservation1 = reservationService.makeReservation(carId1, customerId, startTime, 5);
        Reservation reservation2 = reservationService.makeReservation(carId2, customerId, startTime, 5);
        Reservation reservation3 = reservationService.makeReservation(carId3, customerId, startTime, 5);

        // then
        assertThat(reservation1).isNotNull();
        assertThat(reservation2).isNotNull();
        assertThat(reservation3).isNotNull();
    }


    @RepeatedTest(15)
    void onlyOneReservationIsMadeIfMoreThreadsTryToReserveSameCarForTheSamePeriodOfTime() throws Exception {
        // given
        String carId = "id";
        when(carService.getCar(carId)).thenReturn(new Car(carId, CarType.SEDAN, BigDecimal.valueOf(1)));
        LocalDateTime startTime = LocalDateTime.of(2026, 2, 3, 10, 0);

        // when
        int threads = 10;
        try (ExecutorService executor = Executors.newFixedThreadPool(threads)) {
            List<Future<Boolean>> results = new ArrayList<>();

            for (int i = 0; i < threads; i++) {
                results.add(executor.submit(getMakeReseravationCallable(carId, i, startTime)));
            }
            executor.shutdown();

            // then
            Pair<Long, Long> counts = getCounts(results);
            assertThat(counts.first()).isEqualTo(1);
            assertThat(counts.second()).isEqualTo(threads - 1);
        }
    }

    private Callable<Boolean> getMakeReseravationCallable(String carId, int thread, LocalDateTime startTime) {
        return () -> {
            try {
                reservationService.makeReservation(
                        carId,
                        "customer" + thread,
                        startTime,
                        2
                );
                return true;
            } catch (CarNotAvailableException e) {
                return false;
            }
        };
    }

    private Pair<Long, Long> getCounts(List<Future<Boolean>> results) {
        Map<Boolean, Long> counts = results.stream()
                .map(f -> {
                    try {
                        return f.get();
                    } catch (Exception e) {
                        return false;
                    }
                })
                .collect(Collectors.groupingBy(b -> b, Collectors.counting()));

        return new Pair<>(counts.getOrDefault(true, 0L), counts.getOrDefault(false, 0L));
    }
}