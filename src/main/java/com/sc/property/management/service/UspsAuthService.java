package com.sc.property.management.service;

import com.sc.property.management.dto.TokenResponse;
import com.sc.property.management.util.PropertyConstants;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class UspsAuthService {

  private final WebClient webClient;

  @Value("${usps.base.url}")
  private String uspsBaseUrl;

  @Value("${usps.client.id}")
  private String clientId;

  @Value("${usps.client.secret}")
  private String clientSecret;

  public UspsAuthService(WebClient.Builder webClientBuilder) {
    this.webClient = webClientBuilder.build();
  }

  public String getAuthToken() {
    return webClient
        .post()
        .uri(uspsBaseUrl + PropertyConstants.TOKEN_GENERATION_PATH)
        .header("Content-Type", "application/json")
        .bodyValue(createRequestBody())
        .retrieve()
        .bodyToMono(TokenResponse.class)
        .map(TokenResponse::getAccess_token)
        .block();
  }

  private String createRequestBody() {
    return String.format(
        "{\"client_id\": \"%s\", \"client_secret\": \"%s\", \"grant_type\": \"client_credentials\"}",
        clientId, clientSecret);
  }
}
