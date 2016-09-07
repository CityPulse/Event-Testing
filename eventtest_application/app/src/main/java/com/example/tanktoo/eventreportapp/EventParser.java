package com.example.tanktoo.eventreportapp;

/**
 * Created by tanktoo on 11/08/16.
 */
public class EventParser {
    Parser parser = new Parser();

    public Event parseEvent(String strEvent){
        Event event = new Event();

        Graph eventGraph = this.parser.parse(strEvent);
//        System.out.println(eventGraph.toN3());
        event.setIdentifier(eventGraph.identifier);
        event.setEventClass(eventGraph.type);
        event.setType(eventGraph.attributeList.get("sao:hasType").value);
        event.setSource(eventGraph.attributeList.get("ec:hasSource").value);
        event.setLevel(Integer.parseInt(eventGraph.attributeList.get("sao:hasLevel").value));
        event.setDate(eventGraph.attributeList.get("tl:time").value);
        event.setLatitude(Double.parseDouble(eventGraph.childList.get("sao:hasLocation").attributeList.get("geo:lat").value));
        event.setLongitude(Double.parseDouble(eventGraph.childList.get("sao:hasLocation").attributeList.get("geo:lon").value));
        return event;
    }

    public String parseEvent(Event event){
        Graph eventGraph = new Graph();
        eventGraph.addPrefix("geo", "http://www.w3.org/2003/01/geo/wgs84_pos#");
        eventGraph.addPrefix("sao", "http://purl.oclc.org/NET/UNIS/sao/sao#");
        eventGraph.addPrefix("tl", "http://purl.org/NET/c4dm/timeline.owl#");
        eventGraph.addPrefix("xsd", "http://www.w3.org/2001/XMLSchema#");
        eventGraph.addPrefix("prov", "http://www.w3.org/ns/prov#");
        eventGraph.addPrefix("ec", "http://purl.oclc.org/NET/UNIS/sao/ec#");
        eventGraph.setIdentifierType(event.getIdentifier(), event.getEventClass());
        eventGraph.addAttribute("ec:hasSource", event.getSource());
        eventGraph.addAttribute("tl:time", "dateTime", event.getStringDate());
        eventGraph.addAttribute("sao:hasLevel", "long", String.valueOf(event.getLevel()));
        eventGraph.addAttribute("sao:hasType", "class", event.getType());
        Graph child = new Graph();
        child.setIdentifierType("", "geo:Instant");
        child.addAttribute("geo:lat", "double", String.valueOf(event.getLatitude()));
        child.addAttribute("geo:lon", "double", String.valueOf(event.getLongitude()));
        eventGraph.addChild("sao:hasLocation", child);
        return eventGraph.toN3();
    }
}


/**
 *
 @prefix geo:   <http://www.w3.org/2003/01/geo/wgs84_pos#> .
 @prefix sao:   <http://purl.oclc.org/NET/UNIS/sao/sao#> .
 @prefix tl:    <http://purl.org/NET/c4dm/timeline.owl#> .
 @prefix xsd:   <http://www.w3.org/2001/XMLSchema#> .
 @prefix prov:  <http://www.w3.org/ns/prov#> .
 @prefix ec:    <http://purl.oclc.org/NET/UNIS/sao/ec#> .
 sao:8cc174eb-7015-47ce-9843-b8eaf97b1a5f
 a                ec:TrafficJam ;
 ec:hasSource     "SENSOR_7e1f83b4-ab1e-57dd-8d4c-2c867e8d9141" ;
 sao:hasLevel     "1"^^xsd:long ;
 sao:hasLocation  [ a        geo:Instant ;
 geo:lat  "56.170338772309165"^^xsd:double ;
 geo:lon  "10.197859543568597"^^xsd:double
 ] ;
 sao:hasType      ec:TransportationEvent ;
 tl:time          "2016-07-15T20:56:58.512Z"^^xsd:dateTime .
 */