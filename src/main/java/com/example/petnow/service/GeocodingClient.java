package com.example.petnow.service;

import java.util.Optional;

public interface GeocodingClient {

    Optional<GeocodedCoordinates> geocode(String roadAddress);
}
