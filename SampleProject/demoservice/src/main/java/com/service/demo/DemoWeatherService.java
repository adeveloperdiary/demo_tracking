package com.service.demo;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Random;

@RestController
public class DemoWeatherService {
    @RequestMapping(value = "/weather", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<String> getWeatherDetails(@RequestParam(value = "lat") double lat,
                                                    @RequestParam(value = "lon") double lon,
                                                    @RequestParam(value = "start") long start,
                                                    @RequestParam(value = "appid") String appid,
                                                    @RequestParam(value = "type") String type,
                                                    @RequestParam(value = "cnt") int cnt,
                                                    HttpServletRequest request) {

        String[] weather_desc = {"rainy", "stormy", "sunny", "cloudy", "windy", "typhoons", "sand-storms", "snow-storms", "tornados", "foggy", "snow", "thundersnow", "hail", "sleet", "wildfire", "blizzard", "avalanche", "mist", "thunderstorms"};
        Random rand = new Random();
        int n = rand.nextInt(weather_desc.length);


        String template = "{\"message\":\"\",\"cod\":\"200\",\"city_id\":4887398,\"calctime\":0.0863,\"cnt\":1,\"list\":[{\"main\":{\"temp\":268.987,\"temp_min\":268.987,\"temp_max\":268.987,\"pressure\":1001.11,\"sea_level\":1024.68,\"grnd_level\":1001.11,\"humidity\":100},\"wind\":{\"speed\":5.06,\"deg\":291.002},\"clouds\":{\"all\":48},\"weather\":[{\"id\":802,\"main\":\"" + weather_desc[n] + "\",\"icon\":\"03d\"}],\"dt\":1485703465}]}";


        return new ResponseEntity<>(template, HttpStatus.OK);
    }
}
