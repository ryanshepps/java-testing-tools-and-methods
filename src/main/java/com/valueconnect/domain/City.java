package com.valueconnect.domain;

public class City {

    private String name;
    private Region region;

    public String getName() {
        return this.name;
    }

    public Region getRegion() {
        return this.region;
    }

    public City orElse(Object object) {
        return null;
    }
    
}
