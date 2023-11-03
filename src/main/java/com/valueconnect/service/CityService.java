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
import com.valueconnect.service.GoogleMapsService;
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
    private GoogleMapsService googleMapsService;

    /**
     * Save a city.
     *
     * @param cityDTO the entity to save
     * @return the persisted entity
     */
    public CityDTO save(CityDTO cityDTO) {
        log.debug("Request to save City : {}", cityDTO);
        City city = cityMapper.cityDTOToCity(cityDTO);
        city = cityRepository.save(city);
        CityDTO result = cityMapper.cityToCityDTO(city);
        citySearchRepository.save(city);
        return result;
    }

    /**
     *  Get all the cities.
     *
     *  @param pageable the pagination information
     *  @return the list of entities
     */
    public List<City> findAll(String query, Long provinceId, Long regionId, Boolean hideDisabled) {
        log.debug("Request to get all Cities");

        BooleanExpression predicate = QCity.city.isNotNull();

        if (query != null && query.trim().length() > 0) {
            predicate = QCity.city.name.like(query + "%");
        }
        if (provinceId != null && provinceId > 0) {
            predicate = predicate.and(QCity.city.region.province.id.eq(provinceId));
        }
        if (hideDisabled != null && hideDisabled) {
            predicate = predicate.and(QCity.city.disabled.eq(false));
        }
        if (regionId != null && regionId > 0) {
            predicate = predicate.and(QCity.city.region.id.eq(regionId));
        }
        return cityRepository.findAll(predicate);
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

    /**
     *  Delete the  city by id.
     *
     *  @param id the id of the entity
     */
    public void delete(Long id) {
        log.debug("Request to delete City : {}", id);
        cityRepository.deleteById(id);
        citySearchRepository.deleteById(id);
    }

    /**
     * Compare the city info found in the database (based on province id and city name)
     * against the city info returned by the Google Geocoding API (based on postal code).
     * Favour the Google response when there is a province mismatch.
     */
    public CityDTO findByNameAndProvinceOrPostal(String cityName, Long provinceId, String postCode) {

        Iterable<City> iterableCitiesFromDB = cityRepository.findAll(QCity.city.region.province.id.eq(provinceId)
            .and(QCity.city.name.eq(cityName))
            .and(QCity.city.disabled.eq(false)));

        List<City> listOfCitiesFromDB = new ArrayList<>();
        for (City elem : iterableCitiesFromDB) {
            listOfCitiesFromDB.add(elem);
        }

        City cityFromDB = iterableCitiesFromDB.iterator().hasNext() ? iterableCitiesFromDB.iterator().next() : null;
        Address addressFromGoogle = googleMapsService.getAddressFromPostCode(postCode);

        String fullInfoString = String.format("input cityName='%s', input provinceId=%s, input postCode='%s', " +
                "listOfCitiesFromDB.size()=%s, listOfCitiesFromDB='%s', " +
                "cityFromDB='%s', addressFromGoogle='%s', " +
                "Logged in user: id=%s, login='%s', authorities: %s, integrationId=%s",
            cityName, provinceId, postCode,
            listOfCitiesFromDB.size(), listOfCitiesFromDB,
            cityFromDB, addressFromGoogle,
            (SecurityUtils.getCurrentUser() != null) ? SecurityUtils.getCurrentUser().getId() : "null",
            (SecurityUtils.getCurrentUser() != null) ? SecurityUtils.getCurrentUser().getEmail() : "null",
            (SecurityUtils.getCurrentUser() != null) ? SecurityUtils.getCurrentUser().getUserAuthorities()
                .stream()
                .map(UserAuthority::getAuthority)
                .map(Authority::getName)
                .collect(Collectors.toList()) : "null",
            SecurityUtils.getCurrentUserIntegrationId());

        if (cityFromDB == null && addressFromGoogle == null) {

            log.error("findByNameAndProvinceOrPostal(): " +
                "Worst case failure: Both the database lookup and the Google Geocoding API failed. " +
                "Returning NULL to caller. {}", fullInfoString);
            return null;

        } else if (cityFromDB == null) {

            log.warn("findByNameAndProvinceOrPostal(): " +
                "The database lookup failed, but the Google Geocoding API succeeded. " +
                "Returning addressFromGoogle.getCity(). {}", fullInfoString);
            // We assume that (addressFromGoogle.getCity() != null).
            return cityMapper.cityToCityDTO(addressFromGoogle.getCity());

        } else if (addressFromGoogle == null) {

            log.warn("findByNameAndProvinceOrPostal(): " +
                "The Google Geocoding API failed, but the database lookup succeeded. " +
                "Returning cityFromDB. {}", fullInfoString);
            return cityMapper.cityToCityDTO(cityFromDB);

        } else {
            // Both the database lookup and the Google Geocoding API succeeded.
            // We assume that (addressFromGoogle.getCity() != null).

            if (cityFromDB.getName().equals(addressFromGoogle.getCity().getName())) {
                // The city name matches between the two sources.

                if (cityFromDB.getRegion() != null &&
                    cityFromDB.getRegion().getProvince() != null &&
                    addressFromGoogle.getProvince() != null) {
                    // It is possible to compare province between the two sources.

                    if (cityFromDB.getRegion().getProvince().getAbbrev().equals(addressFromGoogle.getProvince().getAbbrev()) ||
                        cityFromDB.getRegion().getProvince().getName().equals(addressFromGoogle.getProvince().getName())) {

                        // Normal case: Both city name and province name/abbreviation agree between the two sources.
                        // We return the city obtained from the database.

                        // Note: If we have more than one city with the same name in the same province,
                        // we still need to figure out how to break the tie.
                        // Currently we just return the first element from the list because we don't have any means of
                        // linking the region name stored in our database with the ADMINISTRATIVE_AREA_LEVEL_2
                        // returned by Google. They are fundamentally different concepts, so they cannot be compared directly.

                        if (listOfCitiesFromDB.size() > 1) {
                            log.warn("findByNameAndProvinceOrPostal(): " +
                                "There is more than one city name with the same province in the database. " +
                                "Returning cityFromDB (the first element from the list). {}", fullInfoString);
                        }
                        return cityMapper.cityToCityDTO(cityFromDB);

                    } else {

                        log.warn("findByNameAndProvinceOrPostal(): " +
                            "City name matches but province does not match. " +
                            "Returning addressFromGoogle.getCity(). {}", fullInfoString);
                        return cityMapper.cityToCityDTO(addressFromGoogle.getCity());
                    }

                } else {

                    log.warn("findByNameAndProvinceOrPostal(): " +
                        "We are missing necessary data. We cannot compare province between the two sources. " +
                        "Returning addressFromGoogle.getCity(). {}", fullInfoString);
                    return cityMapper.cityToCityDTO(addressFromGoogle.getCity());
                }

            } else {

                log.warn("findByNameAndProvinceOrPostal(): " +
                    "The city name differs between the database and the Google Geocoding API. " +
                    "Returning addressFromGoogle.getCity(). {}", fullInfoString);
                return cityMapper.cityToCityDTO(addressFromGoogle.getCity());
            }
        }

    }

    public City findByPostal(String postCode) {
        Address googleAddress = googleMapsService.getAddressFromPostCode(postCode);
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
