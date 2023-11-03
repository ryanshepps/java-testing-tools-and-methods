package com.valueconnect.domain;


public class Address {

    private City city;
    private Province province;

    public City getCity() {
        return this.city;
    }

    public Province getProvince() {
        return this.province;
    }

    public String getCityName() {
        return this.city.getName();
    }
    
}
