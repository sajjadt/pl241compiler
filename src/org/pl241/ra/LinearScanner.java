package org.pl241.ra;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.pl241.ra.Allocation.Type;

public class LinearScanner {
	public List<LiveInterval> active ;
	public List<LiveInterval> inactive ;
	public List<LiveInterval> handled ;
	public List<LiveInterval> unhandled ;
	private int numRegisters ;
	private Map<String,Integer> allocation ;
	public LinearScanner(Map<String,LiveInterval> intervals, int _numrRegisters){
		numRegisters = _numrRegisters; 
		active = new ArrayList<LiveInterval>();
		inactive = new ArrayList<LiveInterval>();
		handled = new ArrayList<LiveInterval>();
		unhandled = new ArrayList<LiveInterval>(intervals.values());
		Iterator<LiveInterval> lit = unhandled.iterator();
		while( lit.hasNext() ){
			LiveInterval interval = lit.next();
			if ( interval.getRanges().size() == 0 ){
				System.out.println("Remove range.size = 0 " + interval.varName);
				lit.remove();
			}
		}
		Collections.sort(unhandled);
		allocation = new HashMap<String,Integer>();
	}
	public void scan(){
		while ( ! unhandled.isEmpty() ) {
			LiveInterval current = unhandled.get(0);
			unhandled.remove(0);
			
			int position = current.start() ;
			Iterator<LiveInterval> it = active.iterator();
			while (it.hasNext())
		    {
				LiveInterval interval  = it.next();
		        if( interval.finish() < position )
		        {
		        	handled.add( interval );
		            it.remove();
		        }
		        else if( !interval.isAlive(position))
		        {
		        	inactive.add(interval);
		            it.remove();
		        }      
		    }
			it = inactive.iterator();
			while ( it.hasNext()  )
		    {
				 LiveInterval interval  = it.next();
				 if( interval.finish() < position )
			        {
			        	handled.add( interval );
			            it.remove();
			        }
			        else if( interval.isAlive(position) )
			        {
			        	active.add( interval );
			            it.remove();
			        }
		    }
			try{
				tryAllocateFreeRegister(current, position);
			} catch( AllocationFailedException e ){
				allocateBlockedRegister(current, position);
			}
			if( current.allocatedLocation != null ){
				active.add(current);
			}
			
		}
	}
	// Allocation without spilling
	private void tryAllocateFreeRegister(LiveInterval current, int position) throws AllocationFailedException{
		
		// Reg is free to be allocated until this time = inf
		ArrayList<Integer> freeUntilPosition = new ArrayList<Integer>();	
		for(int i = 0 ;i < numRegisters ; ++i ){
			freeUntilPosition.add( Integer.MAX_VALUE) ;
		}
		
		// Set allocated registers busy
		for (LiveInterval interval: active)
		{
			if( interval.allocatedLocation.type == Type.REGISTER ){
				freeUntilPosition.set( interval.allocatedLocation.address , 0 );
			}
		}
		
		for (LiveInterval interval : inactive)
		{
			if( interval.allocatedLocation.type == Type.REGISTER ){
				if(interval.intersects(current) ){
					int nextIntersection =  interval.nextIntersection(current, position); //TODO
					freeUntilPosition.set( interval.allocatedLocation.address , nextIntersection );
				}else {
					freeUntilPosition.set( interval.allocatedLocation.address , Integer.MAX_VALUE );
				}
			}
		}
		
		int bestReg = 0 ;
		for( int i = 0 ;i < numRegisters ; ++i){
			if( freeUntilPosition.get(i) > freeUntilPosition.get(bestReg)){
				bestReg = i ;
			}
		}
		
		if( freeUntilPosition.get(bestReg) == 0 ){
			// Allocation Failed
			throw new AllocationFailedException();
		} else if ( current.finish() < freeUntilPosition.get(bestReg) ){
			// Reg available for whole interval
			current.allocatedLocation = new Allocation(Type.REGISTER, bestReg );
		} else {
			// register available for the first part of the interval
			current.allocatedLocation =  new Allocation(Type.REGISTER, bestReg );
			
			// Split current before freeUntilPosition.get(bestReg)
			// TODO
		}
	}
	//Allocation with spilling
	private void allocateBlockedRegister(LiveInterval current, int position){
		
		// Next usage of the register
		ArrayList<Integer> nextUsePosition = new ArrayList<Integer>();	
		for(int i = 0 ;i < numRegisters ; ++i ){
			nextUsePosition.add( Integer.MAX_VALUE) ;
		}
		
		for(LiveInterval interval: active){
			if( interval.allocatedLocation.type == Type.REGISTER ){
				int nextUse = interval.nextUseAfter(position);
				nextUsePosition.set(interval.allocatedLocation.address, nextUse );
			}
		}
		
		for(LiveInterval interval: inactive){
			if( interval.allocatedLocation.type == Type.REGISTER ){
				if( interval.intersects(current) ){
					int nextUse = interval.nextUseAfter(position);
					nextUsePosition.set(interval.allocatedLocation.address, nextUse );
				}
			}
		}
		
		int bestReg = 0 ;
		for( int i = 0 ;i < numRegisters ; ++i){
			if( nextUsePosition.get(i) > nextUsePosition.get(bestReg)){
				bestReg = i ;
			}
		}
		
		if( current.getFirstUsage() >  nextUsePosition.get(bestReg)	 ){
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
	public Map<String,Integer> getAllocation(){
		return allocation; 
	}
}
