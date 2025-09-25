/**
 * Copyright 2024 Richard Rodgers
 * SPDX-Licence-Identifier: Apache-2.0
 */
package schengen.shortstay;

import java.util.ArrayList;
import java.util.List;

record Result(boolean success, String msg, List<String> pArgs) {
    public Result(boolean success, String msg) {
        this(success, msg, List.of());
    }
}

public class Plan {
    static final int SCHENGEN_PERIOD = 180;
    static final int SCHENGEN_MAXSTAY = 90;

    private List<Stay> stays = new ArrayList<>();

    public Plan() {
    }

    public Plan(List<Stay> stays) {
        this.stays = stays;
    }

    private boolean disjoint(Stay stay) {
        return stays.stream()
               .filter(s -> !s.precedes(stay) && !s.follows(stay))
               .toList().isEmpty();
    }

    static int totalDays(List<Stay> stays) {
        return stays.stream().mapToInt(s -> s.length()).sum();
    }

    List<Stay> stayList() {
        return stays;
    }

    int days() {
        return totalDays(stays);
    }

    Result canInsert(Stay stay) {
        // 2 conditions - stay must be disjoint and total including stay not exceed 90 days
        Result result;
        if (disjoint(stay)) {
            var windowStart = stay.endDate().minusDays(SCHENGEN_PERIOD);
            var range = stays.stream()
                        .filter(s -> s.startDate().isAfter(windowStart) &&
                                     s.startDate().isBefore(stay.startDate()))
                        .toList();
            var overStay = totalDays(range) + stay.length() - SCHENGEN_MAXSTAY;
            if (overStay > 0) {
                result = new Result(false, "errLimit", List.of(String.valueOf(overStay)));
            } else {
                result = new Result(true, "errNone");
            }

        } else {
            result = new Result(false, "errOverlap");
        }
        return result;
    }

    Result insertStay(Stay stay) {
        Result result = canInsert(stay);
        if (result.success()) {
            var windowStart = stay.endDate().minusDays(SCHENGEN_PERIOD);
            var range = stays.stream()
                        .filter(s -> s.startDate().isAfter(windowStart) &&
                                     s.startDate().isBefore(stay.startDate()))
                        .toList();
            var next = stays.stream().filter(s -> s.follows(stay)).findFirst();
            // determine where in list to insert - append if latest stay
            var index = 0;
            if (stays.size() == 0 || next.isEmpty()) {
                index = stays.size();
            } else if (range.isEmpty()) {
                index = 0;
            } else {
                index = stays.indexOf(next.get());
            }
            stays.add(index, stay);
            return new Result(true, String.valueOf(index));
        } else {
            return new Result(false, result.msg());
        }
    }

    void removeStay(int index) {
        stays.remove(index);
    }
}

