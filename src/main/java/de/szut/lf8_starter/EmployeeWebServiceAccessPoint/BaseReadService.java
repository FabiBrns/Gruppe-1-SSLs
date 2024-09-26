package de.szut.lf8_starter.EmployeeWebServiceAccessPoint;

import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public abstract class BaseReadService<TGet, TId> {
    private RestTemplate restTemplate;
    private AccessTokenRetrieverService accessTokenRetrieverService;

    public BaseReadService(String apiUrl,
                           AccessTokenRetrieverService accessTokenRetrieverService,
                           Class<TGet> getDtoType,
                           Class<TGet[]> getAllDtoType) {
        this.apiUrl = apiUrl;
        this.accessTokenRetrieverService = accessTokenRetrieverService;
        this.getDtoType = getDtoType;
        this.getAllDtoType = getAllDtoType;
        this.restTemplate = new RestTemplate();
    }

    private String apiUrl;
    private final Class<TGet> getDtoType;
    private final Class<TGet[]> getAllDtoType;

    protected abstract String getEndpoint();

    private String getFullApiUrl() {
        return apiUrl + "/" + getEndpoint();
    }

    private String getFullApiUrlWithId(TId id) {
        return getFullApiUrl() + "/" + id;
    }

    public TGet GetRequest(TId id) {
        try {
            return restTemplate.exchange(getFullApiUrlWithId(id), HttpMethod.GET, accessTokenRetrieverService.getAccessTokenHttpRequestObject(), getDtoType).getBody();
        }
        catch (Exception e) {
            return null;
        }
    }

    public TGet[] GetAllRequest() {
        try {
            return restTemplate.exchange(getFullApiUrl(), HttpMethod.GET, accessTokenRetrieverService.getAccessTokenHttpRequestObject(), getAllDtoType).getBody();
        }
        catch (Exception e) {
            return null;
        }
    }
}
