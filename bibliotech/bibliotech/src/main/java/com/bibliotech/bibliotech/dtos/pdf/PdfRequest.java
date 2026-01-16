package com.bibliotech.bibliotech.dtos.pdf;

import java.util.Map;

/**
 * Conforms to pdf-microservice OpenAPI: uses `templateName`, optional `fileName`, and `data`.
 */
public record PdfRequest(String templateName, String fileName, Map<String,Object> data) {}
