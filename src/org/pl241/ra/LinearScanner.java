package org.pl241.ra;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.pl241.ra.Allocation.Type;

class LinearScanner {

	public LinearScanner (Map<String,LiveInterval> intervals, int _numRegisters) {
		numRegisters = _numRegisters;

		active = new ArrayList<>();
		inactive = new ArrayList<>();
		handled = new ArrayList<>();
		unhandled = new ArrayList<>(intervals.values());

		Iterator<LiveInterval> lit = unhandled.iterator();

		while (lit.hasNext()) {
			LiveInterval interval = lit.next();
			if (interval.getRanges().size() == 0) {
				System.out.println("Remove range.size = 0 " + interval.varName);
				lit.remove();
			}
		}

		Collections.sort(unhandled);
	}

	void scan() {
		while (!unhandled.isEmpty()) {
			LiveInterval current = unhandled.get(0);
			unhandled.remove(0);
			
			int position = current.start();
			Iterator<LiveInterval> it = active.iterator();
			while (it.hasNext()) {

				LiveInterval interval = it.next();
		        if (interval.finish() < position) {
		        	handled.add(interval);
		            it.remove();
		        } else if(!interval.isAlive(position)) {
		        	inactive.add(interval);
		            it.remove();
		        }      
		    }

			it = inactive.iterator();
			while (it.hasNext()) {
				 LiveInterval interval = it.next();
				 if( interval.finish() < position) {
                     handled.add(interval);
                     it.remove();
                }
                else if (interval.isAlive(position)) {
                    active.add(interval);
                    it.remove();
                }
		    }

			try {
				tryAllocateFreeRegister(current, position);
			} catch( AllocationFailedException e ){
				allocateBlockedRegister(current, position);
			}

			if (current.allocatedLocation != null && current.allocatedLocation.type == Type.REGISTER) {
				active.add(current);
			}
		}
	    // TODO: verify this
		handled.addAll(active);
		handled.addAll(inactive);
	}

	// Allocation without spilling
	private void tryAllocateFreeRegister(LiveInterval current, int position) throws AllocationFailedException {
		
		// Reg is free to be allocated until this time = inf
		ArrayList<Integer> freeUntilPosition = new ArrayList<>();
		for (int i = 0; i <= numRegisters; ++i) {
			freeUntilPosition.add(Integer.MAX_VALUE);
		}
		
		// Set allocated registers busy
		for (LiveInterval interval: active) {
			if (interval.allocatedLocation.type == Type.REGISTER) {
				freeUntilPosition.set(interval.allocatedLocation.address, 0);
			}
		}

		/* TODO: not needed
		for (LiveInterval interval: inactive) {
			if ( interval.allocatedLocation.type == Type.REGISTER) {
				if (interval.intersects(current)) {
					int nextIntersection = interval.nextIntersection(current, position);
					freeUntilPosition.set(interval.allocatedLocation.address, nextIntersection);
				} else {
					freeUntilPosition.set(interval.allocatedLocation.address, Integer.MAX_VALUE);
				}
			}
		}
		*/

		int bestReg = 1;
		for (int i = 1; i <= numRegisters; ++i) {
			if (freeUntilPosition.get(i) > freeUntilPosition.get(bestReg)) {
				bestReg = i;
			}
		}

		if (freeUntilPosition.get(bestReg) == 0) {
			// Allocation has failed
			throw new AllocationFailedException();
		} else if (current.finish() < freeUntilPosition.get(bestReg)) {
			// Reg available for whole interval
			current.allocatedLocation = new Allocation(Type.REGISTER, bestReg);
		} else {
			// register available for the first part of the interval
			current.allocatedLocation =  new Allocation(Type.REGISTER, bestReg);
			LiveInterval newInterval = current.split(freeUntilPosition.get(bestReg));
			unhandled.add(newInterval);
            Collections.sort(unhandled);
		}
	}

	//Allocation with spilling
	private void allocateBlockedRegister(LiveInterval current, int position) {
		
		// Next usage of the register
		ArrayList<Integer> nextUsePosition = new ArrayList<>();
		for (int i = 0; i <= numRegisters; ++i) {
			nextUsePosition.add( Integer.MAX_VALUE) ;
		}

		for (LiveInterval interval: active) {
			if ( interval.allocatedLocation.type == Type.REGISTER) {
				int nextUse = interval.nextUseAfter(position);
				nextUsePosition.set(interval.allocatedLocation.address, nextUse);
			}
		}


		/* TODO not necessary
		for(LiveInterval interval: inactive) {
			if( interval.allocatedLocation.type == Type.REGISTER) {
				if( interval.intersects(current)) {
					int nextUse = interval.nextUseAfter(position);
					nextUsePosition.set(interval.allocatedLocation.address, nextUse);
				}
			}
		}
		*/

		int bestReg = 1;
		for (int i = 1; i < numRegisters; ++i) {
			if (nextUsePosition.get(i) > nextUsePosition.get(bestReg)) {
				bestReg = i ;
			}
		}
		
		if (current.getFirstUsage() > nextUsePosition.get(bestReg)) {
			// all other intervals are used before current,
			// so it is best to spill current itself
			// assign spill slot to current
			// TODO split current before its first use position that requires a register
		} else {
			//current.allocatedLocation = bestReg;
			//
			// split active interval for reg at position
			// split any inactive interval for reg at the end of its lifetime hole
			// make sure that current does not intersect with
			// the fixed interval for reg
		}
		
		// if current intersects with the fixed interval for reg then
		// split current before this intersection

	}

    private List<LiveInterval> active ;
    private List<LiveInterval> inactive ;
    List<LiveInterval> handled ;
    private List<LiveInterval> unhandled ;

    private int numRegisters ;
}
