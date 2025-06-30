package com.itm.space.backendresources;

import com.itm.space.backendresources.annotation.WithMockOAuth2User;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerTest {

    @Autowired
    private MockMvc mvc;

    UUID newAtributUser = UUID.randomUUID();

//    @Container
//    private static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(DockerImageName.parse("postgres:13.5"));
//
//    @DynamicPropertySource
//    static void configureProperties(DynamicPropertyRegistry registry) {
//        registry.add("spring.datasource.url", postgres::getJdbcUrl);
//        registry.add("spring.datasource.username", postgres::getUsername);
//        registry.add("spring.datasource.password", postgres::getPassword);
//        registry.add("spring.jpa.generate-ddl", () -> true);
//    }

    @Test
//    @SneakyThrows
    void authPageTest() {
        String url = "http://localhost:8080/auth/";
        ResponseEntity<String> response = new TestRestTemplate()
                .getForEntity(url, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @WithMockUser(roles = "MODERATOR")
    @SneakyThrows
    void createUserTest() {

        String request = """
                            { "username": "andrey%s",
                            "email": "test%s@ya.com",
                "password": "1234",
                "lastName": "lastName",
                "firstName": "firstName"                        }
                """.formatted(newAtributUser.toString().substring(0, 2), newAtributUser.toString().substring(0, 2));
        MvcResult mvcResult = mvc.perform(post("/api/users") // добавляем юзера
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isOk()).andReturn();
        mvcResult = mvc.perform(post("/api/users")  // при повторной попытке добавить, сервер даёт ошибку
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isConflict()).andReturn();

    }

    @Test
    @WithMockUser(roles = "MODERATOR")
    @SneakyThrows
    void getUserByIdModeratorRole() {
        String idUserAndrey = "94ef93fe-cdc1-4ce0-8058-b4c6de7d4481";
        mvc.perform(get("/api/users/{id}", idUserAndrey))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Андрей"))
                .andExpect(jsonPath("$.lastName").value("Бойко"))
                .andExpect(jsonPath("$.email").value("andrey253@yandex.ru"));

    }

    @Test
    @WithMockUser(roles = "USER")
    @SneakyThrows
    void getUserByIdUserRole() {
        String idUserAndrey = "94ef93fe-cdc1-4ce0-8058-b4c6de7d4481";
        ResultActions resultActions = mvc.perform(get("/api/users/{id}", idUserAndrey))
                .andExpect(status().isForbidden());

    } @Test
    @WithMockUser(roles = "USER")
    @SneakyThrows
    void helloPageTestUserRole() {

        ResultActions resultActions = mvc.perform(get("/hello"))
                .andExpect(status().isNotFound());

    }
    @Test
    @WithMockOAuth2User(username = "admin")
    @SneakyThrows
    void helloPageTestModeratorRole() {

        ResultActions resultActions = mvc.perform(get("/hello"))
                .andExpect(status().isOk());

    }

}


