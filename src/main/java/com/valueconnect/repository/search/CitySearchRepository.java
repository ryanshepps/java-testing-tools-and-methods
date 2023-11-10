package com.valueconnect.repository.search;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.valueconnect.domain.City;

@Repository
public class CitySearchRepository {

    public void save(City city) {
    }

    public void deleteById(Long id) {
    }

    public static Object queryStringQuery(String query) {
        return null;
    }

    public List<City> search(Object queryStringQuery) {
        return null;
    }
    
}
