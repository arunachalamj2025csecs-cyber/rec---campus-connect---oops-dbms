package edu.rec.campusconnect.rules;

import edu.rec.campusconnect.common.Validation;
import java.time.LocalDate;

/**
 * Pure rules for campus events. Dates are stored as text "YYYY-MM-DD".
 */
public final class EventRules {

    private EventRules() {
    }

    /** Parses a YYYY-MM-DD date, or returns null if it is not a valid date. */
    public static LocalDate parseOrNull(String isoDate) {
        return Validation.isIsoDate(isoDate) ? LocalDate.parse(isoDate) : null;
    }

    /** An event is upcoming when it is today or later. */
    public static boolean isUpcoming(String isoDate, LocalDate today) {
        LocalDate date = parseOrNull(isoDate);
        return date != null && !date.isBefore(today);
    }

    /**
     * Sort order for the events board: upcoming events first (soonest first),
     * then past events (most recent first). Invalid dates always go last.
     */
    public static int boardOrder(String dateA, String dateB, LocalDate today) {
        boolean upcomingA = isUpcoming(dateA, today);
        boolean upcomingB = isUpcoming(dateB, today);
        if (upcomingA != upcomingB) {
            return upcomingA ? -1 : 1;
        }
        LocalDate a = parseOrNull(dateA) != null ? parseOrNull(dateA) : LocalDate.MIN;
        LocalDate b = parseOrNull(dateB) != null ? parseOrNull(dateB) : LocalDate.MIN;
        return upcomingA ? a.compareTo(b) : b.compareTo(a);
    }
}
