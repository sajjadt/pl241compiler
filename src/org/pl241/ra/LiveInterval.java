package org.pl241.ra;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class LiveInterval implements Comparable<LiveInterval> {

    LiveInterval(String _varName, boolean _useRegister) {
		varName = _varName ;
		useRegister = _useRegister;
		ranges = new ArrayList<>();
		referencesList = new ArrayList<>();
	}

	private class Range implements Comparable<Range> {
		int start ;
        int finish;
		public Range(int _start , int _finish) {
			start = _start ;
			finish = _finish; 
		}
		@Override
		public int compareTo(Range o) {
			return start - o.start;
		}
    }

    int start() {
		return ranges.get(0).start ;
	}
    int finish() {
		return ranges.get(ranges.size()-1).finish;
	}
    void addReference(int position) {
		referencesList.add(position);
		Collections.sort(referencesList);
	}
	
    Integer nextIntersection(LiveInterval other , int position) {
		ListIterator<Range> it =  ranges.listIterator(0);
		ListIterator<Range> ot = other.ranges.listIterator(0);
		while (it.hasNext() && it.next().finish < position) {
			
		}
		while (ot.hasNext() && ot.next().finish < position) {
		}
		it.previous();
		ot.previous();
		while (it.hasNext() && ot.hasNext()) {
			Range rit = it.next() ;
			Range rot = ot.next() ;
			if (rit.finish < rot.start) {
				ot.previous();
			} else if (rit.start > rot.finish) {
				it.previous();
			} else {
				return Math.max( position, Math.max(rit.start, rot.start) );
			}
		}
		return null;
	}

    void addRange(int start, int end) {
		ListIterator<Range> it =  ranges.listIterator(0);
		boolean alreadyCovered = false ;
		Range newRange = new Range(-1,-1);
		while ( it.hasNext() ){
			Range r = it.next() ;
			boolean removeIt = false ;
			if (r.start <= start && r.finish >= start) {
				removeIt = true ;
				start = r.start ;
			}		
			if (r.start <= end && r.finish >= end) {
				removeIt = true ;
				end = r.finish ;
			}
			if (r.start <= start && r.finish >= end) {
				// already covered
				alreadyCovered = true ;
				break ;
			}
			if (start <= r.start && end >= r.finish) {
				removeIt = true ;
			}
			if (removeIt)
				it.remove();
		}
		if (!alreadyCovered)
			ranges.add(new Range(start,end));
		// Sort by start 
		Collections.sort(ranges);
	}

	void setFrom(int _from) {
		if (ranges.isEmpty()) {
			// Error
		} else {
			for (Range range:ranges) {
				if (range.start < _from && range.finish > _from) {
					range.start = _from ;
				}
			}
			//ranges.get(0).start = _from ;
		}
	}

	@Override 
	public String toString() {
		StringBuilder ret = new StringBuilder("Declared @" + definitionPoint + " References@=");
		for(int ref: referencesList) {
			ret.append(ref).append(",");
		}
		ret.append("Ranges=");
		for(Range range: ranges) {
			ret.append(range.start).append(":").append(range.finish).append(",");
		}
		return ret.toString();
	}
	
	public List<Range> getRanges() {
		return ranges;
	}
	
	@Override
	public int compareTo(LiveInterval o) {
		return start() - o.start();
	}

    Integer nextUseAfter(int start) {
		for (int ref : referencesList) {
			if( ref >= start )
				return ref;
		}
		return Integer.MAX_VALUE;
	}

	public boolean intersects(LiveInterval current) {
        return nextIntersection(current, current.start()) != null;
	}

    Integer getFirstUsage() {
		if( referencesList.size() >0 )
			return referencesList.get(0);
		else 
			return null;
	}
	
    LiveInterval split(int position) {
		LiveInterval newInterval = new LiveInterval(varName, false);
		
		Iterator<Integer> refIterator = referencesList.iterator();
		while (refIterator.hasNext()) {
			int ref = refIterator.next();
			if (ref >= position) { //TODO = case ?
				newInterval.referencesList.add(ref);
				refIterator.remove();
			}
		}
		
		Iterator<Range> rangeIterator = ranges.iterator() ;
		while (rangeIterator.hasNext()) {
			Range range = rangeIterator.next();
			if (range.start >= position ) { //TODO = case ?
				newInterval.ranges.add(range);
				rangeIterator.remove();
			}
		}
		return newInterval;
	}

    boolean isAlive(int time) {
		for (Range range: ranges) {
			if (range.start <= time && range.finish > time)
				return true;
		}
		return false;
	}

    public Allocation allocatedLocation;
    private List<Range> ranges; // Sorted
    public String varName;
    public List<Integer> referencesList;
    public Integer definitionPoint;
    boolean useRegister;
}
