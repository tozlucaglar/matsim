/* *********************************************************************** *
 * project: org.matsim.*
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2014 by the members listed in the COPYING,        *
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

package playground.johannes.gsv.synPop.data;

import java.io.IOException;
import java.util.Map;

import playground.johannes.sna.gis.Zone;
import playground.johannes.sna.gis.ZoneLayer;
import playground.johannes.socialnetworks.gis.io.ZoneLayerSHP;

/**
 * @author johannes
 *
 */
public class LandUseDataLoader implements DataLoader {

	public static final String KEY = "landuse";
	
	private final String file;
	
	private final String populationKey;
	
	public LandUseDataLoader(String file, String populationKey) {
		this.file = file;
		this.populationKey = populationKey;
	}
	
	@Override
	public Object load() {
		try {
			ZoneLayer<Map<String, Object>> zoneLayer = ZoneLayerSHP.read(file);
			
			for(Zone<Map<String, Object>> zone : zoneLayer.getZones()) {
				zone.getAttribute().put(LandUseData.POPULATION_KEY, zone.getAttribute().get(populationKey));
			}
			
			LandUseData data = new LandUseData(zoneLayer);
			
			return data;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

}
