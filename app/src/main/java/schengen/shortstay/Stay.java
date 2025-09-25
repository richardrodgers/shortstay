/**
 * Copyright 2024 Richard Rodgers
 * SPDX-Licence-Identifier: Apache-2.0
 */
package schengen.shortstay;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Stay - a continuous sequence of days
 */
record Stay(String name, LocalDate startDate, LocalDate endDate) {
    public Stay {
        if (! (endDate.isEqual(startDate) || endDate.isAfter(startDate))) {
            throw new IllegalArgumentException("errOrder");
        }
        if (daysBetween(startDate, endDate) > Plan.SCHENGEN_MAXSTAY) {
            throw new IllegalArgumentException("errLength");
        }
    }

    static int daysBetween(LocalDate startDate, LocalDate endDate) {
        return Long.valueOf(ChronoUnit.DAYS.between(startDate, endDate) + 1).intValue();
    }

    int length() {
        return daysBetween(startDate, endDate);
    }

    boolean precedes(Stay stay) {
        return endDate.isBefore(stay.startDate);
    }

    boolean follows(Stay stay) {
        return startDate.isAfter(stay.endDate);
    }
}

