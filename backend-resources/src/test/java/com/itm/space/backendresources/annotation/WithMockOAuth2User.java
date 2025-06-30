package com.itm.space.backendresources.annotation;

import org.springframework.security.test.context.support.WithSecurityContext;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = MockOAuth2UserSecurityContextFactory.class)
public @interface WithMockOAuth2User {
    String username() default "user";
    String[] authorities() default {};
}
