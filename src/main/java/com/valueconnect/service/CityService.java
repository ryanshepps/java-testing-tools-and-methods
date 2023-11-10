package com.valueconnect.service;

import com.valueconnect.domain.Address;
import com.valueconnect.domain.City;
import com.valueconnect.domain.generated.QCity;
import com.valueconnect.domain.authority.Authority;
import com.valueconnect.domain.authority.UserAuthority;
import com.valueconnect.repository.CityRepository;
import com.valueconnect.repository.domain.BooleanExpression;
import com.valueconnect.repository.search.CitySearchRepository;
import com.valueconnect.security.SecurityUtils;
import com.valueconnect.service.MapService;
import com.valueconnect.web.rest.dto.CityDTO;
import com.valueconnect.web.rest.mapper.CityMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service Implementation for managing City.
 */
@Service
public class CityService {

    private final Logger log = LoggerFactory.getLogger(CityService.class);

    @Autowired
    private CityRepository cityRepository;
    @Autowired
    private CityMapper cityMapper;
    @Autowired
    private CitySearchRepository citySearchRepository;
    @Autowired
    private MapService mapsService;

    public void saveToCityRepo(CityDTO cityDTO) {
        log.debug("Request to save City to repo: {}", cityDTO);
        City city = cityMapper.cityDTOToCity(cityDTO);
        city = cityRepository.save(city);
    }

    public void saveToCitySearchRepo(CityDTO cityDTO) {
        log.debug("Request to save City to search repo: {}", cityDTO);
        City city = cityMapper.cityDTOToCity(cityDTO);
        citySearchRepository.save(city);
    }

    public List<City> findAll(String searchString, Long provinceId, Long regionId, Boolean hideDisabled) {
        log.debug("Request to get all Cities");
    
        BooleanExpression query = buildQuery(searchString, provinceId, regionId, hideDisabled);
        
        return cityRepository.findAll(query);
    }
    
    private BooleanExpression buildQuery(String searchString, Long provinceId, Long regionId, Boolean hideDisabled) {
        BooleanExpression query = QCity.city.isNotNull();
    
        if (searchString != null && searchString.trim().length() > 0) {
            query = query.and(QCity.city.name.like(searchString + "%"));
        }
        if (provinceId != null && provinceId > 0) {
            query = query.and(QCity.city.region.province.id.eq(provinceId));
        }
        if (hideDisabled != null && hideDisabled) {
            query = query.and(QCity.city.disabled.eq(false));
        }
        if (regionId != null && regionId > 0) {
            query = query.and(QCity.city.region.id.eq(regionId));
        }
    
        return query;
    }

    /**
     *  Get one city by id.
     *
     *  @param id the id of the entity
     *  @return the entity
     */
    public City findOne(Long id) {
        log.debug("Request to get City : {}", id);
        return cityRepository.findById(id).orElse(null);
    }

    public void deleteFromRepo(Long id) {
        log.debug("Request to delete City : {} from repo", id);
        cityRepository.deleteById(id);
    }

    public void deleteFromSearchRepo(Long id) {
        log.debug("Request to delete City : {} from search repo", id);
        citySearchRepository.deleteById(id);
    }

    public City findByNameAndProvince(String name, long provinceId) {

        Iterable<City> iterableCitiesFromDB = cityRepository.findAll(QCity.city.region.province.id.eq(provinceId)
            .and(QCity.city.name.eq(name))
            .and(QCity.city.disabled.eq(false)));

        List<City> listOfCitiesFromDB = new ArrayList<>();
        for (City elem : iterableCitiesFromDB) {
            listOfCitiesFromDB.add(elem);
        }

        City cityFromDB = iterableCitiesFromDB.iterator().hasNext() ? iterableCitiesFromDB.iterator().next() : null;

        if (cityFromDB == null) {
            log.warn("findByNameAndProvince(): " +
                "The database lookup failed, returning NULL"); 
            return null;
        }

        return cityFromDB;
    }

    public City findByPostal(String postCode) {
        Address googleAddress = mapsService.getAddressFromPostCode(postCode);
        Iterable<City> cities = cityRepository.findAll(QCity.city.region.province.name.eq(googleAddress.getProvince().getName())
            .and(QCity.city.name.eq(googleAddress.getCityName()))
            .and(QCity.city.disabled.eq(false)));
        City newCity = cities.iterator().hasNext() ? cities.iterator().next() : null;
        if (newCity == null) {
            log.warn("findByPostal(): Encountered failure for postCode='{}'. " +
                    "googleAddress.getProvince().getName()='{}', " +
                    "googleAddress.getCityName()='{}'",
                postCode,
                (googleAddress.getProvince() != null) ? googleAddress.getProvince().getName() : "null",
                googleAddress.getCityName());
        }
        return newCity;
    }

    /**
     * Search for the city corresponding to the query.
     *
     *  @param query the query of the search
     *  @return the list of entities
     */
    public List<City> search(String query) {
        log.debug("Request to search for a page of Cities for query {}", query);
        return citySearchRepository.search(CitySearchRepository.queryStringQuery(query));
    }
}
