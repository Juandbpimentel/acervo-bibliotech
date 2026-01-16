package com.bibliotech.bibliotech.clients;

import com.bibliotech.bibliotech.dtos.pdf.PdfRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.util.retry.Retry;

import java.time.Duration;

@Component
public class PdfMicroserviceClient {
    private static final Logger log = LoggerFactory.getLogger(PdfMicroserviceClient.class);

    private final WebClient webClient;
    private final Duration timeout;
    private final int retryAttempts;

    public PdfMicroserviceClient(WebClient.Builder webClientBuilder,
                                 @Value("${pdf.microservice.base-url:http://localhost:3000}") String baseUrl,
                                 @Value("${pdf.microservice.timeout-seconds:30}") long timeoutSeconds,
                                 @Value("${pdf.microservice.retry-attempts:3}") int retryAttempts) {
        this.webClient = webClientBuilder.baseUrl(baseUrl).build();
        this.timeout = Duration.ofSeconds(timeoutSeconds);
        this.retryAttempts = retryAttempts;
    }

    public byte[] generatePdf(PdfRequest request) {
        try {
            return webClient.post()
                    .uri("/generate-pdf") // atualizado para o endpoint conforme OpenAPI
                    .bodyValue(request)
                    .retrieve()
                    .onStatus(status -> status.is5xxServerError(), resp -> {
                        log.warn("PDF microservice returned server error: status={}", resp.statusCode());
                        return resp.createException();
                    })
                    .bodyToMono(byte[].class)
                    .timeout(timeout)
                    .retryWhen(Retry.backoff(retryAttempts, Duration.ofSeconds(2))
                            .filter(throwable -> {
                                // Only retry on server (5xx) errors or non-HTTP exceptions (timeouts, network issues)
                                if (throwable instanceof WebClientResponseException w) {
                                    return w.getStatusCode().is5xxServerError();
                                }
                                return true; // other exceptions are retryable
                            })
                            .doBeforeRetry(retrySignal -> log.warn("Retrying PDF generation: attempt {}/{}; cause={}",
                                    retrySignal.totalRetries() + 1, retryAttempts, retrySignal.failure() == null ? "unknown" : retrySignal.failure().toString()))
                    )
                    .block();
        } catch (WebClientResponseException e) {
            log.error("PDF microservice request failed: status={}, body={}", e.getRawStatusCode(), e.getResponseBodyAsString());
            throw e;
        } catch (Exception e) {
            log.error("Unhandled error calling PDF microservice: {}", e.toString(), e);
            throw e;
        }
    }
}
