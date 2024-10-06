package com.example.demo.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class JwtExceptionFilter extends HttpFilter {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException {
        try {
            chain.doFilter(request, response);
        } catch (MalformedJwtException e) {
            log.error("TokenInvalidException: {}", e.getMessage());
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "MalformedJwtException "+e.getMessage());
        } catch (ExpiredJwtException e) {
            log.error("TokenExpiredException: {}", e.getMessage());
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "ExpiredJwtException "+e.getMessage());
        } catch(UnsupportedJwtException e){
            log.error("UnsupportedJwtException: {}", e.getMessage());
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "UnsupportedJwtException "+e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected Exception: {}", e.getMessage());
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unexpected Exception "+e.getMessage());
        }
    }

    private void sendErrorResponse(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        String jsonResponse = objectMapper.writeValueAsString(message);
        response.getWriter().write(jsonResponse);
    }
}
