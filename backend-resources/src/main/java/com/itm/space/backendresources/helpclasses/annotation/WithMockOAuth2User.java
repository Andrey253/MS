package com.itm.space.backendresources.helpclasses.annotation;

import java.lang.annotation.*;
import org.springframework.security.test.context.support.*;

@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithMockOAuth2UserSecurityContextFactory.class)
public @interface WithMockOAuth2User {
    String username() default "test-user";
    String[] authorities() default {};
}
