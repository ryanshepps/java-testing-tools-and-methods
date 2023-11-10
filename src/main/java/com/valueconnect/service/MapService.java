package com.valueconnect.service;

import org.springframework.stereotype.Service;

import com.valueconnect.domain.Address;
import com.valueconnect.service.Interfaces.IMapService;

@Service
public class MapService implements IMapService{

    public Address getAddressFromPostCode(String postCode) {
        // makes HTTP calls to google API
        // doesn't return full address -- returns province, city, country 
        return null;
    }
    
}
