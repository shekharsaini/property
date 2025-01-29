package com.sc.property.management.service;

import com.sc.property.management.dto.AddressValidationRequest;
import com.sc.property.management.util.PropertyConstants;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@Slf4j
public class AddressValidationService {

  private final WebClient webClient;

  @Autowired private UspsAuthService uspsAuthService;

  @Value("${usps.base.url}")
  private String uspsBaseUrl;

  public AddressValidationService(WebClient.Builder webClientBuilder) {
    this.webClient = webClientBuilder.build();
  }

  public boolean validateAddress(AddressValidationRequest addressValidationRequest) {

    String token = uspsAuthService.getAuthToken();
    String addressValidationPath = PropertyConstants.ADDRESS_VALIDATION_PATH;
    try {
      String encodedStreetAddress =
          URLEncoder.encode(addressValidationRequest.getStreet(), StandardCharsets.UTF_8);
      String encodedCity =
          URLEncoder.encode(addressValidationRequest.getCity(), StandardCharsets.UTF_8);
      String encodedState =
          URLEncoder.encode(addressValidationRequest.getState(), StandardCharsets.UTF_8);
      String uri =
          UriComponentsBuilder.fromHttpUrl(uspsBaseUrl)
              .path(addressValidationPath)
              .queryParam("streetAddress", encodedStreetAddress)
              .queryParam("secondaryAddress", addressValidationRequest.getSecondaryAddress())
              .queryParam("city", encodedCity)
              .queryParam("state", encodedState)
              .toUriString();

      System.out.println("Generated URI: " + uri);
      return Boolean.TRUE.equals(
          webClient
              .get()
              .uri(uri)
              .header("accept", "application/json")
              .header("authorization", "Bearer " + token)
              .retrieve()
              .toEntity(String.class)
              .map(
                  response -> {
                    return response.getStatusCode().is2xxSuccessful();
                  })
              .block());
    } catch (Exception e) {
      return false;
    }
  }
}
