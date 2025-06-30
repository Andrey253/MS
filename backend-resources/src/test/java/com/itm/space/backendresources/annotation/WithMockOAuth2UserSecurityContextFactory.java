package com.itm.space.backendresources.annotation;

import org.springframework.security.core.*;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.*;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.test.context.support.*;

import java.util.Map;

public class WithMockOAuth2UserSecurityContextFactory implements WithSecurityContextFactory<WithMockOAuth2User> {
    @Override
    public SecurityContext createSecurityContext(WithMockOAuth2User annotation) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();

        // Создаём OAuth2User с authorities
        OAuth2User principal = new DefaultOAuth2User(
                AuthorityUtils.createAuthorityList(annotation.authorities()),
                Map.of("sub", annotation.username()),
                "sub"
        );

        // Эмулируем OAuth2 аутентификацию
        context.setAuthentication(
                new OAuth2AuthenticationToken(
                        principal,
                        principal.getAuthorities(),
                        "client-id"
                )
        );

        return context;
    }
}}