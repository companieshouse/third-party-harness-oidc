package uk.gov.companieshouse.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import uk.gov.companieshouse.exception.RestTemplateResponseErrorHandler;
import uk.gov.companieshouse.service.ApiService;

import java.util.Collections;

@Service
public class ApiServiceImpl implements ApiService {

    @Value("${api-uri}")
    protected String apiUri;

    private final RestTemplate restTemplate;
    private static final String AUTH_HEADER = "Authorization";
    private static final String BEARER_HEADER = "Bearer ";

    @Autowired
    public ApiServiceImpl(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.errorHandler(new RestTemplateResponseErrorHandler())
                .build();
    }

    @Override
    public ResponseEntity<String> getApiResponse(String accessToken) {

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.ALL));
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add(AUTH_HEADER, BEARER_HEADER + accessToken);
        HttpEntity<String> request = new HttpEntity<>(headers);

        return restTemplate.exchange(apiUri, HttpMethod.GET, request, String.class);
    }
}
