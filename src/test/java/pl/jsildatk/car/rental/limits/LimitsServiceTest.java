package pl.jsildatk.car.rental.limits;

import org.junit.jupiter.api.Test;
import pl.jsildatk.car.rental.Pair;
import pl.jsildatk.car.rental.limits.exceptions.LimitsExceededException;
import pl.jsildatk.car.rental.limits.service.LimitsService;
import pl.jsildatk.car.rental.limits.service.LimitsServiceImpl;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LimitsServiceTest {

    @Test
    void zeroIsReturnedForNotExistingType() {
        // given
        LimitsService<String> service = createService();

        // when
        long result = service.getCurrentLimitForType("notExisting");

        // then
        assertThat(result).isEqualTo(0);
    }

    @Test
    void correctValueIsReturnedForExistingType() {
        // given
        String type = "test";
        long limit = 2;
        LimitsService<String> service = createService(new Pair<>(type, limit));

        // when
        long result = service.getCurrentLimitForType(type);

        // then
        assertThat(result).isEqualTo(limit);
    }

    @Test
    void settingLimitForNotExistingTypeHasNoEffect() {
        // given
        String type = "notExisting";
        LimitsService<String> service = createService();

        // when
        service.changeLimitForType(type, 2);

        // then
        long existingLimit = service.getCurrentLimitForType(type);
        assertThat(existingLimit).isEqualTo(0);
    }

    @Test
    void settingLimitForExistingTypeOverridesLimit() {
        // given
        String type = "type";
        long newLimit = 25;
        LimitsService<String> service = createService(new Pair<>(type, 5L));

        // when
        service.changeLimitForType(type, newLimit);

        // then
        long existingLimit = service.getCurrentLimitForType(type);
        assertThat(existingLimit).isEqualTo(newLimit);
    }

    @Test
    void limitsWillBeExceededForNotExistingType() {
        // given
        LimitsService<String> service = createService();

        // when
        boolean result = service.willExceedLimits("notExisting", 1);

        // then
        assertThat(result).isTrue();
    }

    @Test
    void limitsWillBeExceededForProperScenario() {
        // given
        String type = "test";
        long limit = 2;
        LimitsService<String> service = createService(new Pair<>(type, limit));

        // when
        boolean result = service.willExceedLimits(type, limit + 3);

        // then
        assertThat(result).isTrue();
    }

    @Test
    void limitsWillNotBeExceededForProperScenario() {
        // given
        String type = "test";
        long limit = 3;
        LimitsService<String> service = createService(new Pair<>(type, limit));

        // when
        boolean result = service.willExceedLimits(type, limit - 1);

        // then
        assertThat(result).isFalse();
    }

    @Test
    void exceptionIsThrownWhenLimitsWillBeExceededWhenIncreasingObjectCount() {
        // given
        String type = "test";
        long limit = 3;
        LimitsService<String> service = createService(new Pair<>(type, limit));

        // when & then
        assertThatThrownBy(() -> service.tryIncreaseObjectCountForType(type, limit + 2))
                .isInstanceOf(LimitsExceededException.class)
                .hasMessageContaining("will exceed limit");
    }

    @Test
    void objectCountIsIncreasedWhenLimitsWillNotBeExceeded() {
        // given
        String type = "test";
        long limit = 3;
        LimitsService<String> service = createService(new Pair<>(type, limit));

        // when & then
        assertThatCode(() -> service.tryIncreaseObjectCountForType(type, limit - 1)).doesNotThrowAnyException();
    }

    @Test
    void limitsWillNotBeExceededAfterLimitsChange() {
        // given
        String type = "test";
        long limit = 3;
        LimitsService<String> service = createService(new Pair<>(type, limit));

        assertThatThrownBy(() -> service.tryIncreaseObjectCountForType(type, limit + 2))
                .isInstanceOf(LimitsExceededException.class);

        long newLimit = limit + 10;

        // when
        service.changeLimitForType(type, newLimit);

        // then
        assertThatCode(() -> service.tryIncreaseObjectCountForType(type, newLimit - 1)).doesNotThrowAnyException();
    }

    @Test
    void typesAreHandledCorrectly() {
        // given
        String type1 = "type1";
        String type2 = "type2";
        String type3 = "type3";
        long limit1 = 1;
        long limit2 = 2;
        long limit3 = 3;
        LimitsService<String> service = createService(new Pair<>(type1, limit1), new Pair<>(type2, limit2), new Pair<>(type3, limit3));

        // when
        boolean result1 = service.willExceedLimits(type1, 1);
        boolean result2 = service.willExceedLimits(type2, 3);
        boolean result3 = service.willExceedLimits(type3, 1);

        // then
        assertThat(result1).isTrue();
        assertThat(result2).isTrue();
        assertThat(result3).isFalse();
    }

    private LimitsService<String> createService() {
        return new LimitsServiceImpl<>(List.of());
    }

    @SafeVarargs
    private LimitsService<String> createService(Pair<String, Long>... pairs) {
        return new LimitsServiceImpl<>(List.of(pairs));
    }

}