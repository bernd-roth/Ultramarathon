package de.tadris.fitness.util.event;

import androidx.annotation.NonNull;

import org.greenrobot.eventbus.EventBus;

public interface EventBusMember {
    /**
     * Tell the object which {@link EventBus} instance it should register to.
     *
     * @apiNote An object may only register to one {@link EventBus} instance at a time. Calling this
     * function will make the object unregister from the bus its currently registered to first.
     *
     * @implNote Make sure the object unregisters first, if it is currently registered to any
     * {@link EventBus}.
     *
     * @param eventBus the {@link EventBus} instance the object should register to
     */
    boolean registerTo(@NonNull EventBus eventBus);

    /**
     * Make the object unregister from the {@link EventBus} its currently registered to.
     */
    void unregisterFromBus();
}
