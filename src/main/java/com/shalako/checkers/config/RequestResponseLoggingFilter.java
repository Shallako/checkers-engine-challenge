package com.shalako.checkers.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.stream.Collectors;

/**
 * Filter that logs all HTTP requests and responses.
 */
@Component
@Slf4j
public class RequestResponseLoggingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        // Wrap request and response to cache their content
        ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);

        long startTime = System.currentTimeMillis();

        // Log request details before processing
        logRequestDetails(requestWrapper);

        try {
            // Process the request
            filterChain.doFilter(requestWrapper, responseWrapper);
        } finally {
            // Log response details after processing
            long duration = System.currentTimeMillis() - startTime;
            logResponseDetails(responseWrapper, duration);
            
            // Copy content back to the original response
            responseWrapper.copyBodyToResponse();
        }
    }

    private void logRequestDetails(ContentCachingRequestWrapper request) {
        String queryString = request.getQueryString() != null ? "?" + request.getQueryString() : "";
        String headers = Collections.list(request.getHeaderNames())
                .stream()
                .map(headerName -> headerName + ": " + request.getHeader(headerName))
                .collect(Collectors.joining(", "));
        
        log.info("REQUEST: {} {} {}", request.getMethod(), request.getRequestURI() + queryString, headers);
        
        // Log request body for POST, PUT, etc.
        if (request.getContentLength() > 0) {
            try {
                String requestBody = getContentAsString(request.getContentAsByteArray(), request.getCharacterEncoding());
                log.info("REQUEST BODY: {}", requestBody);
            } catch (UnsupportedEncodingException e) {
                log.warn("Failed to log request body", e);
            }
        }
    }

    private void logResponseDetails(ContentCachingResponseWrapper response, long duration) {
        try {
            String responseBody = getContentAsString(response.getContentAsByteArray(), response.getCharacterEncoding());
            log.info("RESPONSE: {} ({}ms) - {}", response.getStatus(), duration, responseBody);
        } catch (UnsupportedEncodingException e) {
            log.warn("Failed to log response body", e);
        }
    }

    private String getContentAsString(byte[] content, String charset) throws UnsupportedEncodingException {
        if (content.length == 0) return "";
        return new String(content, charset);
    }
}
