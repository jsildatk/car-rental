package pl.jsildatk.car.rental.limits.service;

import pl.jsildatk.car.rental.Pair;
import pl.jsildatk.car.rental.limits.exceptions.LimitsExceededException;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LimitsServiceImpl<T> implements LimitsService<T> {

    private final Map<T, Long> limits;
    private final Map<T, Long> objectCount;

    public LimitsServiceImpl(Collection<Pair<T, Long>> limits) {
        this.limits = new ConcurrentHashMap<>(limits.size());
        this.objectCount = new ConcurrentHashMap<>(limits.size());

        limits.forEach(limit -> {
            this.limits.put(limit.first(), limit.second());
            this.objectCount.put(limit.first(), 0L);
        });
    }

    @Override
    public long getCurrentLimitForType(T type) {
        return limits.getOrDefault(type, 0L);
    }

    @Override
    public void changeLimitForType(T type, long newLimit) {
        limits.computeIfPresent(type, (_, _) -> newLimit);
    }

    @Override
    public void tryIncreaseObjectCountForType(T type, long numberOfObjects) throws LimitsExceededException {
        if (willExceedLimits(type, numberOfObjects)) {
            throw new LimitsExceededException(type.getClass(), getCurrentLimitForType(type));
        }
        objectCount.computeIfPresent(type, (_, currentValue) -> currentValue + numberOfObjects);
    }

    @Override
    public boolean willExceedLimits(T type, long numberOfObjects) {
        return objectCount.getOrDefault(type, 0L) + numberOfObjects >= getCurrentLimitForType(type);
    }
}