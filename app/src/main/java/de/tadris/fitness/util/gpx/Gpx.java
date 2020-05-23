/*
 * Copyright (c) 2020 Jannis Scheibe <jannis@tadris.de>
 *
 * This file is part of FitoTrack
 *
 * FitoTrack is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     FitoTrack is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.tadris.fitness.util.gpx;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.HashMap;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@JacksonXmlRootElement(localName = "gpx")
public class Gpx {

    @JacksonXmlProperty(isAttribute = true)
    String version;

    @JacksonXmlProperty(isAttribute = true)
    String creator;

    Metadata metadata;

    String name;
    private String desc;

    @JacksonXmlElementWrapper(useWrapping = false)
    List<Track> trk;

    // newly added attributes
    private HashMap<String, String> xmlns;
//    private List<Route> rte;
//    private List<Waypoint> wpt;

    public Gpx(){}

    public Gpx(String version, String creator, Metadata metadata, String name, String desc, List<Track> trk) {
        this.version = version;
        this.creator = creator;
        this.metadata = metadata;
        this.name = name;
        this.desc = desc;
        this.trk = trk;
    }

    public String getVersion() {
        return version;
    }

    public String getCreator() {
        return creator;
    }

    public Metadata getMetadata() {
        return metadata;
    }

    public String getName() {
        return name;
    }

    public String getDesc() {
        return desc;
    }

    public List<Track> getTrk() {
        return trk;
    }
}
