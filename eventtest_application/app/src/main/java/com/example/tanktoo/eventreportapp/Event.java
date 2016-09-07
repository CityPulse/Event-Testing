package com.example.tanktoo.eventreportapp;

/**
 * Created by tanktoo on 15.07.2016.
 */


import com.google.android.gms.maps.model.LatLng;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

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

enum EventSourceType {
    USER("USER"),
    SENSOR("SENSOR");
    private String type;

    EventSourceType(String type) {
        this.type = type;
    }

    public String type() {
        return type;
    }
}

public class Event {
    private String identifier;
    private String eventClass;
    private String source;
    private int level;
    private double latitude;
    private double longitude;
    private String type;
    private Date date;

    public String getEventClass() {
        return eventClass;
    }

    public void setEventClass(String eventClass) {
        this.eventClass = eventClass;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Date getDate() {
        return date;
    }

    public String getStringDate() {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        return df.format(this.date);
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public void setDate(String date) {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        try {
            this.date = df.parse(date);
        }catch (ParseException e){
            System.out.println("Exception while parsing date " + date + ": " + e.getMessage());
        }
    }

    public EventSourceType getEventSourceType(){
        String sourcetype = this.source.split("_")[0].toUpperCase();
        return EventSourceType.valueOf(sourcetype);
    }

    public LatLng getLocation(){
        return new LatLng(this.latitude, this.longitude);
    }

}