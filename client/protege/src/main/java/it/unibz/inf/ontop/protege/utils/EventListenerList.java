package it.unibz.inf.ontop.protege.utils;

import it.unibz.inf.ontop.protege.core.OBDAModelManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class EventListenerList<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventListenerList.class);

    private final List<T> listeners = new ArrayList<>();

    public void add(T listener) {
        Objects.requireNonNull(listener);
        if (listeners.contains(listener))
            //throw new IllegalArgumentException("ListenerList " + listeners + " already contains " + listener);
            return;

        listeners.add(listener);
    }

    public void remove(T listener) {
        boolean result = listeners.remove(listener);
        if (!result)
            throw new IllegalArgumentException("ListenerList " + listeners + " did not contain " + listener);
    }

    public void fire(Consumer<? super T> consumer) {
        listeners.forEach(listener -> {
            try {
                consumer.accept(listener);
            }
            catch (Exception e) {
                LOGGER.debug("Badly behaved listener: {}", listener.getClass().toString());
                LOGGER.debug(e.getMessage(), e);
            }
        });
    }
}
