package csp;

import java.time.LocalDate;
import java.util.Set;

import java.util.ArrayList;
import java.util.List;

/**
 * CSP: Calendar Satisfaction Problem Solver
 * Provides a solution for scheduling some n meetings in a given
 * period of time and according to some unary and binary constraints
 * on the dates of each meeting.
 */
public class CSP {

	/**
	 * Method that prunes invalid values from a variables domain
	 * @param meeting Meeting being constrained
	 * @param constraint UnaryConstraint to check consistency of
	 */
	public static boolean nodeConsistency(Meeting meeting, UnaryDateConstraint constraint) {	
		
		switch (constraint.OP) {
        case "==": 
        	if (meeting.domain.contains(constraint.R_VAL)) { // if this is a valid date for the meeting
            	meeting.setDate(constraint.R_VAL);			 // assign it to this date
        	} else {										 // otherwise empty this Meeting's domain
        		meeting.domain = new ArrayList<LocalDate>();
        	}
        	break;
        case "!=": 
        	meeting.domain.remove(constraint.R_VAL);
        	break;
        case ">":  
        	meeting.removeBefore(constraint.R_VAL);
        	meeting.domain.remove(constraint.R_VAL);
        	break;
        case "<":  
        	meeting.removeAfter(constraint.R_VAL);
        	meeting.domain.remove(constraint.R_VAL);
        	break;
        case ">=": 
        	meeting.removeBefore(constraint.R_VAL);
        	break;
        case "<=": 
        	meeting.removeAfter(constraint.R_VAL);
        	break;
        }
		
		return !meeting.domainEmpty();
	}
	
	/**
	 * Method that ensures arc consistency for a BinaryDateConstraint. Returns true if arc is consistent, returns false if tail domain has been reduced to 0
	 * @param tail Meeting on left side of constraint
	 * @param head Meeting on right side of constraint
	 * @param constraint BinaryDateConstraint to ensure consistency with
	 * @return true if consistency has been ensured, false if tail domain has been reduced to 0
	 */
	public static boolean arcConsistency(Meeting tail, Meeting head, BinaryDateConstraint constraint) {
		ArrayList<LocalDate> newTailDomain = new ArrayList<>();
					
		for (LocalDate d : tail.domain) {
			UnaryDateConstraint c = new UnaryDateConstraint(0, constraint.OP, d); // doesn't matter what left value is since nodeConsistency assumes the correct meeting was put in
			Meeting compareHead = new Meeting(head.domain); // preserves domain of head during nodeConsistency check

			if ( nodeConsistency(compareHead, c) ) {
				newTailDomain.add(d);
			} 
		}
		
		tail.setDomain(newTailDomain);
		
		return !tail.domainEmpty();
		
	}

  //TODO: make a method that checks for arc consistency with one constraint
  // for each value in the tail, there exists a value in the head that satisfies it
  // removes values from tail domain that are inconsistent

  //TODO: (possibly) make assignments as nodes whose edges are _____ and whose contents are an assignment

  //TODO: select unassigned var which chooses the variable the smallest size domain (the meetings with the least amount of dates)

    /**
     * Public interface for the CSP solver in which the number of meetings,
     * range of allowable dates for each meeting, and constraints on meeting
     * times are specified.
     * @param nMeetings The number of meetings that must be scheduled, indexed from 0 to n-1
     * @param rangeStart The start date (inclusive) of the domains of each of the n meeting-variables
     * @param rangeEnd The end date (inclusive) of the domains of each of the n meeting-variables
     * @param constraints Date constraints on the meeting times (unary and binary for this assignment)
     * @return A list of dates that satisfies each of the constraints for each of the n meetings,
     *         indexed by the variable they satisfy, or null if no solution exists.
     */
    public static List<LocalDate> solve (int nMeetings, LocalDate rangeStart, LocalDate rangeEnd, Set<DateConstraint> constraints) {
        ArrayList<LocalDate> solution = new ArrayList<>();
        
       // Construct all meetings
        ArrayList<Meeting> meetings = new ArrayList<>();
        for (int i = 0; i < nMeetings; i++) {
        	Meeting newMeeting = new Meeting(rangeStart, rangeEnd);
        	meetings.add(newMeeting);
        }
        
        // node preprocessing
        for (DateConstraint c: constraints) {
        	if (c.arity() == 1) {
        		nodeConsistency( meetings.get(c.L_VAL), (UnaryDateConstraint)c);
        	}
        }
        
        
        //Used to test TODO: delete before submission
        System.out.println("Meetings after node preprocessing: " );
        for (Meeting m : meetings) {
        	System.out.println( "     " + m);
        }
        
        System.out.println("Testing arc consistency method");
        for (DateConstraint c : constraints) {
        	if (c.arity() == 2) {
        		BinaryDateConstraint bC = (BinaryDateConstraint)c;

        		System.out.println( "     Constraint " + c + ( (arcConsistency(meetings.get(bC.L_VAL), meetings.get(bC.R_VAL), bC) ) ? " is consistent" : " is not consistent.") );
        		System.out.println( "          tail: " + meetings.get(bC.L_VAL));
        		System.out.println( "          head: " + meetings.get(bC.R_VAL));

        	}
        }
        
        
        return solution;
    }
    
    // -----------------------------------------------
    // Meeting Variable
    // -----------------------------------------------
    
    /**
     * Meeting class that holds the domain of the meeting as an ArrayList<LocalDate>.
     * It also holds the date the meeting has been assigned to if it has been assigned.
     */
    private static class Meeting {
        
        ArrayList<LocalDate> domain;
        LocalDate date = null;
        
        Meeting (LocalDate start, LocalDate end ) {
            domain = new ArrayList<LocalDate>();
            while (start.isBefore(end)) {
            	domain.add(start);
            	start = start.plusDays(1);
            }
            domain.add(start); // add end date to domain
        }
        
        Meeting(LocalDate date) {
            domain = new ArrayList<LocalDate>();
            domain.add(date);
        	this.date = date;
        }
        
        Meeting(ArrayList<LocalDate> domain) {
        	setDomain(domain);
        }
        
        public boolean isAssigned() {
        	if (domain.size() == 1) {
        		date = domain.get(0);
        	}
        	
            return date != null;
        }
        
        public boolean domainEmpty() {
        	return domain.size() == 0;
        }
        
        public void setDate(LocalDate date) {
        	domain = new ArrayList<LocalDate>();
        	domain.add(date);
        	this.date = date;
        }
        
        public void setDomain(ArrayList<LocalDate> inputDomain) {
        	this.domain = new ArrayList<>();
        	for (LocalDate d : inputDomain) {
        		domain.add(d);
        	}
        }
        
        public boolean removeBefore(LocalDate date) {
        	boolean removed = false;
        	ArrayList<LocalDate> toRemove = new ArrayList<>();
        	
        	for (LocalDate d : domain) {
        		if (d.isBefore(date)) {
        			toRemove.add(d);
        		}
        	}
        	
        	for (LocalDate d : toRemove) {
        		domain.remove(d);
        	}
        	
        	return removed;
        }
        
        public boolean removeAfter(LocalDate date) {
        	boolean removed = false;
        	ArrayList<LocalDate> toRemove = new ArrayList<>();
        	
        	for (LocalDate d : domain) {
        		if (d.isAfter(date)) {
        			toRemove.add(d);
        		}
        	}
        	
        	for (LocalDate d : toRemove) {
        		domain.remove(d);
        	}
        	
        	return removed;
        }
        
        public boolean remove(LocalDate date) {
        	return domain.remove(date);
        }
        
        @Override 
        public String toString() {
        	String datesString = "";
        	for (LocalDate d : domain) {
        		datesString += d + " ";
        	}
        	return datesString;
        }
        
    }

}