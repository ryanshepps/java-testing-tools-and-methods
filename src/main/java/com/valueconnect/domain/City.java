package com.valueconnect.domain;

public class City {

    private String name;
    private Region region;

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Region getRegion() {
        return this.region;
    }

    public void setRegion(Region region) {
        this.region = region;
    }

    public City orElse(Object object) {
        return null;
    }
    
}
