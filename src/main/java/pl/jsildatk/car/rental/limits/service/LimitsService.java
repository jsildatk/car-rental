package pl.jsildatk.car.rental.limits.service;

import pl.jsildatk.car.rental.limits.exceptions.LimitsExceededException;

/**
 * Service for handling limits across the system.
 * By the contract, all operations on not registered types should have no effect.
 *
 * @param <T> type of object for keeping track of limits
 */
public interface LimitsService<T> {

    /**
     * Get current limit for given type.
     *
     * @param type registered type
     * @return registered limit for given type, 0 if type is not registered
     */
    long getCurrentLimitForType(T type);

    /**
     * Change limit for given type in the runtime.
     *
     * @param type     registered type
     * @param newLimit new limit that will be associated with given type
     */
    void changeLimitForType(T type, long newLimit);

    /**
     * Try to increase given object count for given type.
     * This method works for bulk operations - if limits will be exceeded for even only 1 object, nothing will be added.
     * For custom logic use {@link #willExceedLimits(Object, long)} instead.
     *
     * @param type            registered type
     * @param numberOfObjects number of objects that will be added
     * @throws LimitsExceededException if adding given number of objects will exceed the limits
     */
    void tryIncreaseObjectCountForType(T type, long numberOfObjects) throws LimitsExceededException;

    /**
     * Check if adding a certain number of new objects will exceed current limits for given type.
     * Useful for adding a custom logic when limits will/won't be exceeded. If throwing {@link LimitsExceededException} is enough,
     * use {@link #tryIncreaseObjectCountForType(Object, long)} instead.
     *
     * @param type            registered type
     * @param numberOfObjects number of objects that will be checked for limits
     * @return true if adding a new object will exceed configured limits, false otherwise
     */
    boolean willExceedLimits(T type, long numberOfObjects);

}
