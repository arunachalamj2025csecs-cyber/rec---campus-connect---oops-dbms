package edu.rec.campusconnect.rules;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class EventRulesTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 10, 31);

    @Test
    void todayAndLaterAreUpcoming() {
        assertTrue(EventRules.isUpcoming("2026-10-31", TODAY));
        assertTrue(EventRules.isUpcoming("2026-11-01", TODAY));
        assertFalse(EventRules.isUpcoming("2026-10-30", TODAY));
        assertFalse(EventRules.isUpcoming("not-a-date", TODAY));
    }

    @Test
    void boardShowsUpcomingSoonestFirstThenPastNewestFirst() {
        assertTrue(EventRules.boardOrder("2026-11-05", "2026-11-20", TODAY) < 0);
        assertTrue(EventRules.boardOrder("2026-10-01", "2026-09-01", TODAY) < 0);
        assertTrue(EventRules.boardOrder("2026-11-20", "2026-10-01", TODAY) < 0); // upcoming always before past
        assertEquals(0, EventRules.boardOrder("2026-11-05", "2026-11-05", TODAY));
    }

    @Test
    void invalidDatesAreSortedLast() {
        assertTrue(EventRules.boardOrder("2026-11-05", "bad", TODAY) < 0);
        assertTrue(EventRules.boardOrder("2026-10-01", "bad", TODAY) < 0);
        assertNull(EventRules.parseOrNull("bad"));
    }
}
