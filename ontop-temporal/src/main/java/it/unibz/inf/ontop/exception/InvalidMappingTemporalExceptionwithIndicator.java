package it.unibz.inf.ontop.exception;

import java.util.List;

/**
 * Created by elem on 08/08/2017.
 */
public class InvalidMappingTemporalExceptionwithIndicator extends InvalidMappingExceptionWithIndicator {

    public static final int INTERVAL_QUERY_IS_BLANK = 5;

    public InvalidMappingTemporalExceptionwithIndicator(List<Indicator> indicators) {
        super(indicators);
    }
}
