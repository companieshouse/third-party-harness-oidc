package uk.gov.companieshouse.service;

import java.io.IOException;

import uk.gov.companieshouse.model.TokenResponse;
import uk.gov.companieshouse.model.User;

public interface UserAuthService {

    String getAccessToken(String authCode) throws IOException;

    TokenResponse getTokens(String authCode) throws IOException;

    User getUserDetails(String accessToken);

}
