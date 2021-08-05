package uk.gov.companieshouse.model;

public class UserTokenStore {

    private String email;
    private String accessToken;
    private String idToken;

    public UserTokenStore() { }

    public UserTokenStore(String email, String accessToken) {
        this.email = email;
        this.accessToken = accessToken;
    }

    public UserTokenStore(String email, String accessToken, String idToken) {
        this.email = email;
        this.accessToken = accessToken;
        this.idToken = idToken;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getIdToken() { return idToken; }

    public void setIdToken(String idToken) { this.idToken = idToken; }
}
