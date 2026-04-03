package ru.ya.practicum.payment.client.api;

import ru.ya.practicum.payment.client.ApiClient;


import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Arrays;
import java.util.stream.Collectors;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient.ResponseSpec;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;

@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", date = "2026-04-03T17:53:51.165741500+03:00[Europe/Moscow]", comments = "Generator version: 7.21.0")
public class BalanceApi {
    private ApiClient apiClient;

    public BalanceApi() {
        this(new ApiClient());
    }

    public BalanceApi(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public ApiClient getApiClient() {
        return apiClient;
    }

    public void setApiClient(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    /**
     * Получить баланс
     * 
     * <p><b>200</b> - Сумма баланса
     * @param username Уникальное имя пользователя
     * @return Long
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getBalanceRequestCreation(@jakarta.annotation.Nonnull String username) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'username' is set
        if (username == null) {
            throw new WebClientResponseException("Missing the required parameter 'username' when calling getBalance", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("username", username);

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = { 
            "application/json"
        };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] {  };

        ParameterizedTypeReference<Long> localVarReturnType = new ParameterizedTypeReference<Long>() {};
        return apiClient.invokeAPI("/balance/{username}", HttpMethod.GET, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Получить баланс
     * 
     * <p><b>200</b> - Сумма баланса
     * @param username Уникальное имя пользователя
     * @return Long
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<Long> getBalance(@jakarta.annotation.Nonnull String username) throws WebClientResponseException {
        ParameterizedTypeReference<Long> localVarReturnType = new ParameterizedTypeReference<Long>() {};
        return getBalanceRequestCreation(username).bodyToMono(localVarReturnType);
    }

    /**
     * Получить баланс
     * 
     * <p><b>200</b> - Сумма баланса
     * @param username Уникальное имя пользователя
     * @return ResponseEntity&lt;Long&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<Long>> getBalanceWithHttpInfo(@jakarta.annotation.Nonnull String username) throws WebClientResponseException {
        ParameterizedTypeReference<Long> localVarReturnType = new ParameterizedTypeReference<Long>() {};
        return getBalanceRequestCreation(username).toEntity(localVarReturnType);
    }

    /**
     * Получить баланс
     * 
     * <p><b>200</b> - Сумма баланса
     * @param username Уникальное имя пользователя
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getBalanceWithResponseSpec(@jakarta.annotation.Nonnull String username) throws WebClientResponseException {
        return getBalanceRequestCreation(username);
    }

    /**
     * Вычесть из баланса
     * 
     * <p><b>200</b> - Сумма баланса
     * @param username Уникальное имя пользователя
     * @param body The body parameter
     * @return Long
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec paymentRequestCreation(@jakarta.annotation.Nonnull String username, @jakarta.annotation.Nonnull Long body) throws WebClientResponseException {
        Object postBody = body;
        // verify the required parameter 'username' is set
        if (username == null) {
            throw new WebClientResponseException("Missing the required parameter 'username' when calling payment", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // verify the required parameter 'body' is set
        if (body == null) {
            throw new WebClientResponseException("Missing the required parameter 'body' when calling payment", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("username", username);

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = { 
            "application/json"
        };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { 
            "application/json"
        };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] {  };

        ParameterizedTypeReference<Long> localVarReturnType = new ParameterizedTypeReference<Long>() {};
        return apiClient.invokeAPI("/payment/{username}", HttpMethod.POST, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Вычесть из баланса
     * 
     * <p><b>200</b> - Сумма баланса
     * @param username Уникальное имя пользователя
     * @param body The body parameter
     * @return Long
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<Long> payment(@jakarta.annotation.Nonnull String username, @jakarta.annotation.Nonnull Long body) throws WebClientResponseException {
        ParameterizedTypeReference<Long> localVarReturnType = new ParameterizedTypeReference<Long>() {};
        return paymentRequestCreation(username, body).bodyToMono(localVarReturnType);
    }

    /**
     * Вычесть из баланса
     * 
     * <p><b>200</b> - Сумма баланса
     * @param username Уникальное имя пользователя
     * @param body The body parameter
     * @return ResponseEntity&lt;Long&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<Long>> paymentWithHttpInfo(@jakarta.annotation.Nonnull String username, @jakarta.annotation.Nonnull Long body) throws WebClientResponseException {
        ParameterizedTypeReference<Long> localVarReturnType = new ParameterizedTypeReference<Long>() {};
        return paymentRequestCreation(username, body).toEntity(localVarReturnType);
    }

    /**
     * Вычесть из баланса
     * 
     * <p><b>200</b> - Сумма баланса
     * @param username Уникальное имя пользователя
     * @param body The body parameter
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec paymentWithResponseSpec(@jakarta.annotation.Nonnull String username, @jakarta.annotation.Nonnull Long body) throws WebClientResponseException {
        return paymentRequestCreation(username, body);
    }
}
