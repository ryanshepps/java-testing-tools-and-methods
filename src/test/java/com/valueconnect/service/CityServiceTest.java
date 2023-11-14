package com.valueconnect.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import com.valueconnect.domain.City;
import com.valueconnect.domain.Province;
import com.valueconnect.domain.Region;
import com.valueconnect.domain.generated.QCity;
import com.valueconnect.domain.generated.QueryObject;
import com.valueconnect.repository.CityRepository;
import com.valueconnect.repository.domain.BooleanExpression;
import com.valueconnect.service.CityService;

@RunWith(SpringRunner.class)
@SpringBootTest
public class CityServiceTest {

    @Autowired
    private CityService cityService;

    @MockBean
    public CityRepository cityRepository;

    @Test
    public void shouldFindAllCities() {
        Province mockProvince = new Province();
        Region mockRegion = new Region();
        mockRegion.setProvince(mockProvince);
        City mockCity = new City();
        mockCity.setRegion(mockRegion);
        List<City> expected = new ArrayList<>();
        expected.add(mockCity);

        when(this.cityRepository.findAll(any(BooleanExpression.class))).thenReturn(expected);
        List<City> actual = cityService.findAll(null, null, null, null);
        
        assertEquals(expected, actual);
    }
    
}
