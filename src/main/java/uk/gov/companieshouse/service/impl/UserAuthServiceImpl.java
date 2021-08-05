package uk.gov.companieshouse.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import uk.gov.companieshouse.exception.RestTemplateResponseErrorHandler;
import uk.gov.companieshouse.model.TokenResponse;
import uk.gov.companieshouse.model.User;
import uk.gov.companieshouse.service.UserAuthService;

@Service
public class UserAuthServiceImpl implements UserAuthService {

    @Value("${client-id}")
    protected String clientId;

    @Value("${client-secret}")
    protected String clientSecret;

    @Value("${redirect-uri}")
    protected String redirectUri;

    @Value("${token-uri}")
    protected String tokenUri;

    @Value("${user-uri}")
    protected String userUri;

    protected Map<String, TokenResponse> userTokenStoreMap;
    private final RestTemplate restTemplate;
    private UriComponentsBuilder tokenUriTemplate;
    private static final String AUTH_HEADER = "Authorization";
    private static final String BEARER_HEADER = "Bearer ";

    @PostConstruct
    void init() {
        this.tokenUriTemplate = UriComponentsBuilder.fromUriString(tokenUri)
                .query("code={code}")
                .query("client_id="+clientId)
                .query("client_secret="+clientSecret)
                .query("grant_type=authorization_code")
                .query("redirect_uri="+redirectUri);
    }

    @Autowired
    public UserAuthServiceImpl(RestTemplateBuilder restTemplateBuilder) {
        this.userTokenStoreMap = new HashMap<>();
        this.restTemplate = restTemplateBuilder.errorHandler(new RestTemplateResponseErrorHandler())
                .build();
    }

    @Override
    public String getAccessToken(String authCode) throws IOException {
        //Add secret and id to headers
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.ALL));
        HttpEntity<String> request = new HttpEntity<>(headers);

        //Auth server token uri, with access code from logon
        String accessTokenUrl = this.tokenUriTemplate.
                buildAndExpand(authCode, "authorization_code", redirectUri).toUriString();

        ResponseEntity<String> response = restTemplate.exchange(accessTokenUrl, HttpMethod.POST, request, String.class);

        // Get the Access Token From the received JSON response
        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = mapper.readTree(response.getBody());
        return node.path("access_token").asText();
    }

    @Override
    public TokenResponse getTokens(String authCode) {
        TokenResponse tokenResponse = null;
        //Add secret and id to headers
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.ALL));
        //headers.add(AUTH_HEADER, "Basic " + encodedCredentials);
        HttpEntity<String> request = new HttpEntity<>(headers);

        //Auth server token uri, with access code from logon
        String accessTokenUrl = this.tokenUriTemplate.
                buildAndExpand(authCode, "authorization_code", redirectUri).toUriString();

        ResponseEntity<String> response = restTemplate.exchange(accessTokenUrl, HttpMethod.POST, request, String.class);

        // Get the Access Token From the received JSON response
        ObjectMapper mapper = new ObjectMapper();
        try {
            tokenResponse = mapper.readValue(response.getBody(), TokenResponse.class);
            String[] chunks = tokenResponse.getIdToken().split("\\.");
            Base64.Decoder decoder = Base64.getDecoder();
            String payload = new String(decoder.decode(chunks[1]));
            JsonNode node = mapper.readTree(payload);
            tokenResponse.setCompanyNumber(node.path("company").asText());
            tokenResponse.setTokenIssuer(node.path("iss").asText());
            userTokenStoreMap.put(node.path("family_name").asText(), tokenResponse);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return tokenResponse;
    }

    @Override
    public User getUserDetails(String accessToken) {

        HttpHeaders headers = new HttpHeaders();
        headers.add(AUTH_HEADER, BEARER_HEADER + accessToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<User> responseEntity = restTemplate
                .exchange(userUri, HttpMethod.GET, entity, User.class);

        User u = responseEntity.getBody();
        TokenResponse st = userTokenStoreMap.get(u.getEmail());

        System.out.println("User Details: " + responseEntity.getBody().toString());
        return responseEntity.getBody();
    }

}
