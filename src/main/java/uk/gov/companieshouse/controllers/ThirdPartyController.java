package uk.gov.companieshouse.controllers;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import javax.validation.Valid;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.UriComponentsBuilder;
import uk.gov.companieshouse.model.Query;
import uk.gov.companieshouse.model.Scope;
import uk.gov.companieshouse.model.TokenResponse;
import uk.gov.companieshouse.model.User;
import uk.gov.companieshouse.service.UserAuthService;

@Controller
public class ThirdPartyController {
    private static final String SCOPE = "scope";
//    private static final String USER_SCOPE = "https://identity.company-information.service.gov.uk/user/profile.read";
    private static final String USER_SCOPE = "openid profile";

    private final UserAuthService userAuthService;

    @Value("${client-id}")
    private String clientId;
    @Value("${redirect-uri}")
    private String redirectUri;
    @Value("${authorise-uri}")
    private String authoriseUri;

    @Autowired
    public ThirdPartyController(UserAuthService userAuthService) {
        this.userAuthService = userAuthService;
    }

    @GetMapping(value = "/login")
    public String login() {
        return "login";
    }

    @GetMapping(value = "/attemptLogin")
    public String attemptLogin(RedirectAttributes redirectAttributes,
                               @RequestParam(value = "company", required = true) String company) {
        redirectAttributes.addAttribute("response_type", "code");
        redirectAttributes.addAttribute("client_id", clientId);
        redirectAttributes.addAttribute("redirect_uri", redirectUri);
        redirectAttributes.addAttribute(SCOPE, USER_SCOPE);

        String claims = getClaimsParameter(company);
        System.out.println("Claims:" + claims);
        Map m  = new HashMap<String, String>();
        m.put("claims", claims);
        redirectAttributes.addAllAttributes(m);
        System.out.println("Authorize URL:" + authoriseUri + ' ' + redirectAttributes);
        return "redirect:" + authoriseUri;
    }

    @GetMapping(value = "/loginViaRequestedScope")
    public String loginViaCompanyNumber() {
        return "loginRequestedScope";
    }

    @GetMapping(value = "/loginRequestedScope")
    public String attemptLoginCompanyNumber(
            @Valid @ModelAttribute("requestedScope") Scope scope,
            BindingResult result, RedirectAttributes redirectAttributes, Model model) {
        if (result.hasErrors()) {
            List<FieldError> errorsList = result.getFieldErrors();
            System.out.println("HAS ERRORS - " + errorsList);
            model.addAttribute("errors", errorsList);
            return "loginRequestedScope";
        }
        redirectAttributes.addAttribute(SCOPE, scope.getRequestedScope());
        redirectAttributes.addAttribute("response_type", "code");
        redirectAttributes.addAttribute("client_id", clientId);
        redirectAttributes.addAttribute("redirect_uri", redirectUri);
        String claims = getClaimsParameter("1234");
        System.out.println("Claims:" + claims);
        redirectAttributes.addAttribute("claims", claims);
        System.out.println("Authorize URL with company:" + authoriseUri + ' ' + redirectAttributes);
        return "redirect:" + authoriseUri;
    }

    @GetMapping(value = "/redirect")
    public String handleRedirect(@RequestParam("code") String code, Model model)
            throws IOException {
        TokenResponse tokens = userAuthService.getTokens(code);
        if(tokens.getIdToken() == null || tokens.getAccessToken() == null){
            throw new IOException("Could not get a token: " + tokens.getAdditionalProperties());
        }
        System.out.println("TOKENS: " + tokens.toString());
        User user = userAuthService.getUserDetails(tokens.getAccessToken());
        model.addAttribute("user", user);
        model.addAttribute("accessToken", tokens.getAccessToken());
        model.addAttribute("idToken", tokens.getIdToken());
        model.addAttribute("company", tokens.getCompanyNumber());
        model.addAttribute("tokenIssuer", tokens.getTokenIssuer());
        model.addAttribute("query", new Query());
        return "loginResult";
    }

    /**
     * Converts the company number into a OIDC claim JSON structure, which will be added to the
     * call to /authorize as a query parameters.
     * @param companyNo the input company number.
     * @return the JSON string representing the 'claims' object.
     */
    private String getClaimsParameter(String companyNo)  {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode rootNode = mapper.createObjectNode();

        ObjectNode companyNode1 = mapper.createObjectNode();
        companyNode1.put("value", companyNo);

        ObjectNode companyNode2 = mapper.createObjectNode();
        companyNode2.put("value", companyNo);

        ObjectNode userInfoNode = mapper.createObjectNode();
        userInfoNode.set("company", companyNode1);

        ObjectNode idtokenNode = mapper.createObjectNode();
        idtokenNode.set("company", companyNode2);

        rootNode.set("userInfo", userInfoNode);
        rootNode.set("id_token", idtokenNode);

        String jsonString = null;
        try {
            jsonString = mapper.writeValueAsString(rootNode);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return jsonString;
    }
}