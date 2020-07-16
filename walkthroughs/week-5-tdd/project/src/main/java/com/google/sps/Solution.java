package com.google.sps;

import java.util.ArrayList;
import java.util.Collection;

public class Solution {

    protected int numOptionalAttendees;
    protected Collection<TimeRange> solution;
    protected boolean isValidSolution;

    public Solution(int numOptionalAttendees, Collection<TimeRange> solution) {
      this.numOptionalAttendees = numOptionalAttendees;
      this.solution = solution;
      // If the solution list is empty, it couldnt be solved
      this.isValidSolution = solution.size() != 0;
    }

    public Collection<TimeRange> solution() {
      return new ArrayList<TimeRange>(this.solution);
    }

    public int numOptionalAttendees() {
      return this.numOptionalAttendees;
    }

    public static Solution betterSolution(Solution one, Solution two) {
      if (one.isBetterThan(two)) {
        return one;
      }
      return two;
    }

    public boolean isBetterThan(Solution other) {
      if (other == null) {
        return true;
      }

      // If they are both invalid, it doesn't matter which is better
      if (!this.isValidSolution && !other.isValidSolution) {
        return true;
      }

      // If one is valid and the other isnt, the valid one is better.
      if (this.isValidSolution && !other.isValidSolution) {
        return true;
      }
      if (!this.isValidSolution && other.isValidSolution) {
        return false;
      }

      // if we got here they are both valid solutions. Return whichever
      // has more attendees.  if they have the same, return whichever has more times
      if (this.numOptionalAttendees != other.numOptionalAttendees) {
        return this.numOptionalAttendees > other.numOptionalAttendees;
      } else {
        return this.solution.size() >= other.solution.size();
      }
    }
}
