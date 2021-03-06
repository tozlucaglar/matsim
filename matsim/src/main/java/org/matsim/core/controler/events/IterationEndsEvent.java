/* *********************************************************************** *
 * project: org.matsim.*
 * ControlerFinishIterationEvent.java
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

package org.matsim.core.controler.events;

import org.matsim.core.controler.MatsimServices;

/**
 * Event class to notify observers that a iteration is finished
 *
 * @author dgrether
 */
public final class IterationEndsEvent extends AbstractIterationEvent {
	public IterationEndsEvent(MatsimServices services, int iteration, boolean isLastIteration) {
		super(services, iteration, isLastIteration);
	}
}
