# BalanceApi

All URIs are relative to *http://localhost*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**getBalance**](BalanceApi.md#getBalance) | **GET** /balance/{username} | Получить баланс |
| [**payment**](BalanceApi.md#payment) | **POST** /payment/{username} | Вычесть из баланса |



## getBalance

> Long getBalance(username)

Получить баланс

### Example

```java
// Import classes:
import ru.ya.practicum.payment.client.ApiClient;
import ru.ya.practicum.payment.client.ApiException;
import ru.ya.practicum.payment.client.Configuration;
import ru.ya.practicum.payment.client.models.*;
import ru.ya.practicum.payment.client.api.BalanceApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost");

        BalanceApi apiInstance = new BalanceApi(defaultClient);
        String username = "username_example"; // String | Уникальное имя пользователя
        try {
            Long result = apiInstance.getBalance(username);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling BalanceApi#getBalance");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Reason: " + e.getResponseBody());
            System.err.println("Response headers: " + e.getResponseHeaders());
            e.printStackTrace();
        }
    }
}
```

### Parameters


| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **username** | **String**| Уникальное имя пользователя | |

### Return type

**Long**

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Сумма баланса |  -  |


## payment

> Long payment(username, body)

Вычесть из баланса

### Example

```java
// Import classes:
import ru.ya.practicum.payment.client.ApiClient;
import ru.ya.practicum.payment.client.ApiException;
import ru.ya.practicum.payment.client.Configuration;
import ru.ya.practicum.payment.client.models.*;
import ru.ya.practicum.payment.client.api.BalanceApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost");

        BalanceApi apiInstance = new BalanceApi(defaultClient);
        String username = "username_example"; // String | Уникальное имя пользователя
        Long body = 56L; // Long | 
        try {
            Long result = apiInstance.payment(username, body);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling BalanceApi#payment");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Reason: " + e.getResponseBody());
            System.err.println("Response headers: " + e.getResponseHeaders());
            e.printStackTrace();
        }
    }
}
```

### Parameters


| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **username** | **String**| Уникальное имя пользователя | |
| **body** | **Long**|  | |

### Return type

**Long**

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Сумма баланса |  -  |

