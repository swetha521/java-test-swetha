package com.lbg.boardroom.bookings.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lbg.boardroom.bookings.BookingRequestProcessorApp;
import com.lbg.boardroom.bookings.model.Error;
import org.json.JSONException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.TEXT_PLAIN;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT, classes = {BookingRequestProcessorApp.class, RestTemplate.class})
class BookingControllerITest {

    public static final String BOOKING_API = "http://localhost:8080/api/v1/bookings";
    @Autowired
    private RestTemplate client;

    @Test
    void booking_ShouldReturn200_WithExpectedStatus() throws URISyntaxException, IOException, JSONException {
        String inputText = "0900 1730\n" + "2020-01-18 10:17:06 EMP001\n" + "2020-01-21 09:00 2\n" + "2020-01-18 12:34:56 EMP002\n" + "2020-01-21 09:00 2\n" + "2020-01-18 09:28:23 EMP003\n" + "2020-01-22 14:00 2\n" + "2020-01-18 11:23:45 EMP004\n" + "2020-01-22 16:00 1\n" + "2020-01-15 17:29:12 EMP005\n" + "2020-01-21 16:00 3\n" + "2020-01-18 11:00:45 EMP006\n" + "2020-01-23 16:00 1\n" + "2020-01-15 11:00:45 EMP007\n" + "2020-01-23 15:00 2 ";
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode expectedResult = objectMapper.readTree(this.getClass().getClassLoader().getResourceAsStream("response.json"));
        URI uri = new URI(BOOKING_API);
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setAccept(Collections.singletonList(APPLICATION_JSON));
        httpHeaders.setContentType(TEXT_PLAIN);

        ResponseEntity<List> responseEntity = client.exchange(uri, HttpMethod.POST, new HttpEntity<>(inputText, httpHeaders), List.class);

        Assertions.assertSame(responseEntity.getStatusCode(), HttpStatus.OK);
        assertEquals(expectedResult.toString().replaceAll(":\"", "=").replaceAll(":\\[", "=\\[").replaceAll("\"", ""), responseEntity.getBody().toString().replaceAll(" ", ""));
    }

    @Test
    void booking_ShouldReturn400_WhenInvalidRequestPayload() throws URISyntaxException {
        URI uri = new URI(BOOKING_API);
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setAccept(Collections.singletonList(APPLICATION_JSON));
        httpHeaders.setContentType(TEXT_PLAIN);
        try {
            client.exchange(uri, HttpMethod.POST, new HttpEntity<>("0900 1730\n", httpHeaders), Error.class);
            fail();
        } catch (HttpClientErrorException exception) {
            assertEquals(exception.getStatusCode(), HttpStatus.BAD_REQUEST);
            assertEquals("400 : [{\"message\":\"Request payload has insufficient/invalid data for processing. Minimum number of lines expected is 3\"}]", exception.getMessage());
        }
    }

}
