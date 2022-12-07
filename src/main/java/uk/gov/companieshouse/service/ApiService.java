package uk.gov.companieshouse.service;

import org.springframework.http.ResponseEntity;

public interface ApiService {

    ResponseEntity<String> getApiResponse(String accessToken);

}
