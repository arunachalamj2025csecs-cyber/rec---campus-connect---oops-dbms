package edu.rec.campusconnect.service;

import edu.rec.campusconnect.model.Event;
import edu.rec.campusconnect.model.User;
import edu.rec.campusconnect.rules.EventRules;
import edu.rec.campusconnect.view.ComplaintView;
import edu.rec.campusconnect.view.UserView;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import edu.rec.campusconnect.common.ApiBody;

/** Builds the numbers and lists shown on the Dashboard page. */
@Service
public class DashboardService {

    private final ComplaintService complaints;
    private final EventService events;

    public DashboardService(ComplaintService complaints, EventService events) {
        this.complaints = complaints;
        this.events = events;
    }

    public Map<String, Object> build(User viewer) {
        List<ComplaintView> mine = complaints.list(viewer, "all");
        LocalDate today = events.today();
        List<Event> published = events.publishedSorted();
        List<Event> upcoming = published.stream().filter(e -> EventRules.isUpcoming(e.date, today)).toList();
        List<Event> past = published.stream().filter(e -> !EventRules.isUpcoming(e.date, today)).toList();

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("total", mine.size());
        stats.put("open", countStatus(mine, "open"));
        stats.put("inProgress", countStatus(mine, "inprogress"));
        stats.put("resolved", countStatus(mine, "resolved"));
        stats.put("events", upcoming.size());

        return ApiBody.ok()
                .with("user", UserView.from(viewer))
                .with("stats", stats)
                .with("recentComplaints", firstN(mine, 5))
                .with("upcomingEvents", firstN(upcoming, 8))
                .with("recentEvents", firstN(past, 5))
                .with("upcomingOnly", firstN(upcoming, 5))
                .build();
    }

    private static int countStatus(List<ComplaintView> list, String status) {
        return (int) list.stream().filter(c -> status.equals(c.status())).count();
    }

    private static <T> List<T> firstN(List<T> list, int n) {
        return list.stream().limit(n).toList();
    }
}
