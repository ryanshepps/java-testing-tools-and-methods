package com.valueconnect.service.Interfaces;

import com.valueconnect.domain.Address;

public interface IMapService{
    
    public Address getAddressFromPostCode(String postCode);
}
