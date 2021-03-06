// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps;

import java.lang.Math;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public final class FindMeetingQuery {

  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    Solution sol = queryOptimalSolution(events, request);

    // special case- a meeting with only optional attendees, none of whom
    // can make it, should return an empty list
    if (request.optionalAttendeesOnly() && sol.numOptionalAttendees() == 0) {
      return new ArrayList<TimeRange>();
    }
    return sol.solution();
  }

  private Solution queryOptimalSolution(Collection<Event> events, MeetingRequest request) {
    // Precompute mandatory times to avoid recalculating it many times
    Collection<TimeRange> timesMandatory =
        querySpecificAttendees(events, request, request.getAttendees());

    return queryOptimalSolution(
        events,
        timesMandatory,
        new ArrayList<String>(request.getOptionalAttendees()),
        request,
        new ArrayList<String>(),
        0);
  }

  // Use recursive backtracking to generate all combinations and track the best current solution
  private Solution queryOptimalSolution(
      Collection<Event> events,
      Collection<TimeRange> timesMandatory,
      List<String> optionalAttendees,
      MeetingRequest request,
      List<String> attendeeAccumulator,
      int indexAccumulator) {

    Solution bestSol = new Solution(0, timesMandatory);
    for (int i = indexAccumulator; i < optionalAttendees.size(); i++) {
      attendeeAccumulator.add(optionalAttendees.get(i));

      Collection<TimeRange> timesOptional =
          querySpecificAttendees(events, request, attendeeAccumulator);
      Collection<TimeRange> timesBoth =
          enforceCriteria(intersect(timesMandatory, timesOptional), request);

      Solution sol = new Solution(attendeeAccumulator.size(), timesBoth);
      Solution branchSol = queryOptimalSolution(
          events, timesMandatory, optionalAttendees, request, attendeeAccumulator, i + 1);
      bestSol = Solution.betterSolution(bestSol, Solution.betterSolution(sol, branchSol));

      attendeeAccumulator.remove(attendeeAccumulator.size() - 1);
    }

    return bestSol;
  }

  // Solve the query using the given attendees, not the attendees in the request
  private Collection<TimeRange> querySpecificAttendees(
      Collection<Event> events, MeetingRequest request, Collection<String> attendees) {

    List<Collection<TimeRange>> attendeesFreeTimes =
        new ArrayList<>(getAttendeesFreeTimes(events, attendees).values());

    /**
      If there are no participants, the whole day will be returned
      If there are any participants, the intersection of the whole day and
      the participants schedule will be the participants schedule.
    */
    attendeesFreeTimes.add(Arrays.asList(TimeRange.WHOLE_DAY));

    return getAvailableTimes(attendeesFreeTimes, request);
  }

  private Collection<TimeRange> getAvailableTimes(
      Collection<Collection<TimeRange>> attendeesFreeTimes, MeetingRequest request) {

    return enforceCriteria(collapse(attendeesFreeTimes), request);
  }

  private Collection<TimeRange> enforceCriteria(
      Collection<TimeRange> possibleTimes, MeetingRequest request) {

    Collection<TimeRange> options = new ArrayList<TimeRange>();

    for (TimeRange timeRange : possibleTimes) {
      if (timeRange.duration() >= request.getDuration()) {
        options.add(timeRange);
      }
    }

    return options;
  }

  // Collapse multiple sets of timeranges into one collection representing the total intersection
  private Collection<TimeRange> collapse(Collection<Collection<TimeRange>> timeRangesCollection) {
    if (timeRangesCollection.size() == 0) {
      return new TreeSet<TimeRange>(TimeRange.ORDER_BY_START);
    }

    Iterator<Collection<TimeRange>> it = timeRangesCollection.iterator();
    Collection<TimeRange> collapsed = it.next();
    for (Collection<TimeRange> timeRanges : timeRangesCollection) {
      collapsed = intersect(collapsed, timeRanges);
    }

    return collapsed;
  }

  // THe order really doesn't matter because intersection is commutative
  private Collection<TimeRange> intersect(Collection<TimeRange> one, Collection<TimeRange> two) {
    Collection<TimeRange> intersection = new TreeSet<TimeRange>(TimeRange.ORDER_BY_START);

    List<TimeRange> oneList = new ArrayList<>(one);
    List<TimeRange> twoList = new ArrayList<>(two);

    int i = 0;
    int j = 0;
    while (i < oneList.size() && j < twoList.size()) {
      TimeRange oneTimeRange = oneList.get(i);
      TimeRange twoTimeRange = twoList.get(j);

      if (oneTimeRange.overlaps(twoTimeRange)) {
        intersection.add(getOverlap(oneTimeRange, twoTimeRange));
        if (oneTimeRange.end() < twoTimeRange.end()) {
          i++;
        } else {
          j++;
        }
      } else {
        if (oneTimeRange.start() < twoTimeRange.start()) {
          i++;
        } else {
          j++;
        }
      }
    }
    return intersection;
  }

  // Get attendees event times, then take the complement of the set.
  private Map<String, Collection<TimeRange>> getAttendeesFreeTimes(
      Collection<Event> events, Collection<String> attendees) {

    Map<String, Collection<TimeRange>> attendeesFreeTimes = new HashMap<>();
    Map<String, Collection<TimeRange>> attendeesEventTimes =
        getAttendeesEventTimes(events, attendees);

    for (String attendee : attendees) {
      Collection<TimeRange> attendeeEventTimes = attendeesEventTimes.get(attendee);
      Collection<TimeRange> attendeeFreeTimes = new TreeSet<TimeRange>(TimeRange.ORDER_BY_START);

      // Removing any overlapping ranges by merging them.
      attendeeEventTimes = mergeTimeRanges(attendeeEventTimes);

      int startTime = TimeRange.START_OF_DAY;
      for (TimeRange eventTime : attendeeEventTimes) {
        // Assumes none of the events are overlapping.
        TimeRange freeTime = TimeRange.fromStartEnd(startTime, eventTime.start(), false);
        attendeeFreeTimes.add(freeTime);

        startTime = eventTime.end();
      }

      if (startTime != TimeRange.END_OF_DAY) {
        TimeRange freeTime = TimeRange.fromStartEnd(startTime, TimeRange.END_OF_DAY, true);
        attendeeFreeTimes.add(freeTime);
      }

      attendeesFreeTimes.put(attendee, attendeeFreeTimes);
    }

    return attendeesFreeTimes;
  }

  private Map<String, Collection<TimeRange>> getAttendeesEventTimes(
      Collection<Event> events, Collection<String> attendees) {

    Map<String, Collection<TimeRange>> map = new HashMap<>();
    for (String attendee : attendees) {
      map.put(attendee, new TreeSet<TimeRange>(TimeRange.ORDER_BY_START));
    }

    for (Event event : events) {
      for (String attendee : event.getAttendees()) {
        if (map.containsKey(attendee)) {
          map.get(attendee).add(event.getWhen());
        }
      }
    }

    return map;
  }

  private TimeRange getOverlap(TimeRange rangeOne, TimeRange rangeTwo) {
    // Assumes that an overlap exists
    int start = Math.max(rangeOne.start(), rangeTwo.start());
    int end = Math.min(rangeOne.end(), rangeTwo.end());
    return TimeRange.fromStartEnd(start, end, false);
  }

  // Given a collection of time ranges, merge any overlapping ranges into a single large range
  private Collection<TimeRange> mergeTimeRanges(Collection<TimeRange> ranges) {
    List<TimeRange> mergedTimes = new ArrayList<>();

    if (ranges.size() == 0) {
      return mergedTimes;
    }

    TimeRange prev = ranges.iterator().next();
    for (TimeRange range : ranges) {
      if (prev.overlaps(range)) {
        prev = mergeOverlappingRanges(prev, range);
      } else {
        mergedTimes.add(prev);
        prev = range;
      }
    }
    mergedTimes.add(prev);

    return mergedTimes;
  }

  private TimeRange mergeOverlappingRanges(TimeRange rangeOne, TimeRange rangeTwo) {
    // Assumes that an overlap exists
    int start = Math.min(rangeOne.start(), rangeTwo.start());
    int end = Math.max(rangeOne.end(), rangeTwo.end());
    return TimeRange.fromStartEnd(start, end, false);
  }
}
