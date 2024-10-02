package de.szut.lf8_starter.employeeWebServiceAccessPoint;

import de.szut.lf8_starter.employeeWebServiceAccessPoint.Dtos.GetAccessTokenDto;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class AccessTokenRetrieverService {
    private static String accessToken;
    private static LocalDateTime expired_on;
    private RestTemplate restTemplate;
    private String username = "user";
    private String password = "test";
    private String url = "https://keycloak.szut.dev/auth/realms/szut/protocol/openid-connect/token";

    public AccessTokenRetrieverService() {
        this.restTemplate = new RestTemplate();
        List<HttpMessageConverter<?>> messageConverters = new ArrayList<>();

        MappingJackson2HttpMessageConverter jacksonConverter = new MappingJackson2HttpMessageConverter();
        jacksonConverter.setSupportedMediaTypes(Collections.singletonList(MediaType.APPLICATION_JSON));

        messageConverters.add(jacksonConverter);
        messageConverters.add(new StringHttpMessageConverter());
        messageConverters.add(new FormHttpMessageConverter());
        this.restTemplate.setMessageConverters(messageConverters);
    }

    public String getToken() {
        if (accessToken == null || (expired_on != null && expired_on.isBefore(LocalDateTime.now()))) {
            var token = retrieveTokenDto();
            accessToken = token.getAccess_token();
            expired_on = LocalDateTime.now().plusSeconds(token.getExpires_in());
        }
        return accessToken;
    }
    public HttpEntity<MultiValueMap<String, String>> getAccessTokenHttpRequestObject(){
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + getToken());
        return new HttpEntity<>(map, headers);
    }

    private GetAccessTokenDto retrieveTokenDto() {
        MultiValueMap<String, String> map= new LinkedMultiValueMap<>();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        map.add("grant_type", "password");
        map.add("client_id", "employee-management-service");
        map.add("username", username);
        map.add("password", password);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
        var response = restTemplate.exchange(url, HttpMethod.POST, request, GetAccessTokenDto.class);
        if (response.hasBody()) return response.getBody();
        return null;
    }
}
