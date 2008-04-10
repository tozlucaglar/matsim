/* *********************************************************************** *
 * project: org.matsim.*
 * EstimReactiveLinkTT.java
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2007 by the members listed in the COPYING,        *
 *                   LICENSE and WARRANTY file.                            *
 * email           : info at matsim dot org                                *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *   See also COPYING, LICENSE and WARRANTY file                           *
 *                                                                         *
 * *********************************************************************** */

/**
 *
 */
package playground.johannes.eut;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.matsim.events.EventAgentArrival;
import org.matsim.events.EventAgentWait2Link;
import org.matsim.events.EventLinkEnter;
import org.matsim.events.EventLinkLeave;
import org.matsim.events.handler.EventHandlerAgentArrivalI;
import org.matsim.events.handler.EventHandlerAgentWait2LinkI;
import org.matsim.events.handler.EventHandlerLinkEnterI;
import org.matsim.events.handler.EventHandlerLinkLeaveI;
import org.matsim.interfaces.networks.basicNet.BasicLinkI;
import org.matsim.mobsim.QueueLink;
import org.matsim.mobsim.QueueNetworkLayer;
import org.matsim.mobsim.SimulationTimer;
import org.matsim.network.Link;
import org.matsim.plans.Person;
import org.matsim.router.util.TravelTimeI;
import org.matsim.utils.misc.Time;

/**
 * @author illenberger
 *
 */
public class EstimReactiveLinkTT implements
		EventHandlerLinkEnterI,
		EventHandlerLinkLeaveI,
		EventHandlerAgentArrivalI,
		EventHandlerAgentWait2LinkI,
		TravelTimeI {

	private QueueNetworkLayer queueNetwork;
	
	private Map<BasicLinkI, LinkTTCalculator> linkTTCalculators;

	private BasicLinkI lastQueriedLink;

	private double lastQueryTime;

	private double lastTravelTime;

	public EstimReactiveLinkTT(QueueNetworkLayer network) {
		queueNetwork = network;
	}
	
	public void reset(int iteration) {
		this.linkTTCalculators = new LinkedHashMap<BasicLinkI, LinkTTCalculator>();
	}

	public void handleEvent(EventLinkEnter event) {
		increaseCount(event.link, event.agent, event.time);
	}

	public void handleEvent(EventLinkLeave event) {
		decreaseCount(event.link, event.agent, event.time);
	}

	public void handleEvent(EventAgentArrival event) {
		decreaseCount(event.link, event.agent, event.time);
	}

	public void handleEvent(EventAgentWait2Link event) {
		increaseCount(event.link, event.agent, event.time);
	}

	private void increaseCount(Link link, Person person, double time) {
		LinkTTCalculator f = this.linkTTCalculators.get(link);
		if(f == null) {
			f = new LinkTTCalculator(link);
			this.linkTTCalculators.put(link, f);
		}
		f.enterLink(person, time);
	}

	private void decreaseCount(Link link, Person person, double time) {
		LinkTTCalculator f = this.linkTTCalculators.get(link);
		f.leaveLink(person, time);
	}

	public double getLinkTravelTime(Link link, double time) {
		double simtime = SimulationTimer.getTime();
		if ((simtime == this.lastQueryTime) && (link == this.lastQueriedLink))
			return this.lastTravelTime;
		else {
			this.lastQueryTime = simtime;
			this.lastQueriedLink = link;

			LinkTTCalculator f = this.linkTTCalculators.get(link);
			if (f == null)
				this.lastTravelTime = link.getFreespeed(Time.UNDEFINED_TIME);
			else
				/*
				 * TODO: This is ugly!
				 */
				this.lastTravelTime = f.getLinkTravelTime(simtime);

			return this.lastTravelTime;
		}
	}

	private class LinkTTCalculator {

		private final QueueLink qLink;

		private final double freeFlowTravTime;

		private int outCount = 0;

		private double lastEvent = 0;

		private double lastCall = 0;

		private double currentTravelTime;

		private double currentOutFlow;

		private double feasibleOutFlow;

		private SortedSet<Sample> samples;

		public LinkTTCalculator(Link link) {
			this.qLink = queueNetwork.getQueueLink(link.getId());
			
			this.samples = new TreeSet<Sample>();
			this.freeFlowTravTime = link.getFreespeedTravelTime(Time.UNDEFINED_TIME);
			this.currentTravelTime = this.freeFlowTravTime;
			this.feasibleOutFlow = this.qLink.getSimulatedFlowCapacity();
			this.currentOutFlow = this.feasibleOutFlow;
		}

		public void enterLink(Person person, double time) {
			this.samples.add(new Sample(person, time + this.freeFlowTravTime));
		}

		public void leaveLink(Person person, double time) {
			this.outCount++;

//			Sample sample = null;
			/*
			 * Since we can expect that the person is near the head of the set,
			 * this should not be that expensive...
			 */
			for(Sample s : this.samples) {
				if(s.person.equals(person)) {
					this.samples.remove(s);
//					sample = s;
					break;
				}
			}
//			this.samples.remove(sample);

			double deltaT = time - this.lastEvent;
			if(deltaT > 0) {
				this.currentOutFlow = this.outCount/deltaT;
				this.lastEvent = time;
				this.outCount = 0;

				if(this.samples.isEmpty())
					this.feasibleOutFlow = this.qLink.getSimulatedFlowCapacity();
				else if(this.samples.first().linkLeaveTime > time)
					this.feasibleOutFlow = this.qLink.getSimulatedFlowCapacity();
				else
					this.feasibleOutFlow = this.currentOutFlow;
			}
		}

		public double getLinkTravelTime(double time) {
			if (time > this.lastCall) {
				this.lastCall = time;

				if (this.samples.isEmpty())
					this.currentTravelTime = qLink.getLink().getLength() /
											 qLink.getLink().getFreespeed(Time.UNDEFINED_TIME);
				else {
					double tt = this.samples.size() / this.feasibleOutFlow;
					this.currentTravelTime = Math.max(this.freeFlowTravTime, tt);
				}
			}

			return this.currentTravelTime;
		}
	}

	private class Sample implements Comparable<Sample> {

		public Person person;

		public double linkLeaveTime;

		public Sample(Person person, double linkLeaveTime) {
			this.person = person;
			this.linkLeaveTime = linkLeaveTime;
		}

		public int compareTo(Sample o) {
			if(o == null)
				return 1;
			else {
				int result = Double.compare(this.linkLeaveTime, o.linkLeaveTime);
				if(result == 0)
					result = this.person.getId().compareTo(o.person.getId());

				return result;
			}
		}


	}

//	private class TupleComparator implements Comparator<Tuple<Person, Double>> {
//
//		public int compare(Tuple<Person, Double> o1, Tuple<Person, Double> o2) {
//			if(o1 == null)
//				return -1;
//			else if(o2 == null)
//				return 1;
//			else
//				return o1.getSecond().compareTo(o2.getSecond());
//		}
//
//	}
}
