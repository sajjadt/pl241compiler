package org.pl241.ra ;

import org.pl241.ra.LiveInterval;

public class IntervalsTest {
	public static void main(String[] args) {
		// Crate an interval
		LiveInterval int1 = new LiveInterval("v1");
		LiveInterval int2 = new LiveInterval("v2");
		
		
		int1.addRange(7, 11);
		int1.addRange(14, 33);
		int1.addRange(5, 12);
		int1.addRange(12, 14);
		
		
		int1.addReference(7);
		int1.addReference(22);
		int1.addReference(4);
		
		
		int2.addRange(14, 16);
		int2.addRange(12, 13);
		int2.addRange(4, 9);
		int2.addRange(20, 30);
		int2.addReference(6);
		int2.addReference(13);
		int2.addReference(30);
		
		System.out.println("Int1 first usage at:" + int1.getFirstUsage());
		System.out.println("Int2 first usage at:" + int2.getFirstUsage());
		
		System.out.println("Int1,2 first intersects at:" + int1.nextIntersection(int2, int1.start()));
		
		System.out.println("Int1,2 first intersects after 10 at:" + int1.nextIntersection(int2, 10));
		System.out.println("Int1,2 first intersects after 25 at:" + int1.nextIntersection(int2, 25));
		
		System.out.println("Int1 before split: " + int1);
		System.out.println("Int2 before split: " + int2);
		LiveInterval int3 = int2.split(7);
		System.out.println("Int2: " + int2);
		System.out.println("Int3: " + int3);
		
	}
}
