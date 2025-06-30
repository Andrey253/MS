package com.itm.space.backendresources;

import com.itm.space.backendresources.annotation.WithMockOAuth2User;
import com.itm.space.backendresources.api.response.UserResponse;
import com.itm.space.backendresources.controller.UserController;
import com.itm.space.backendresources.service.UserService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jdk.jshell.spi.ExecutionControl;
import net.bytebuddy.matcher.FilterableList;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithSecurityContext;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class IntegrationTest extends BaseIntegrationTest {
    @MockBean
    private UserService userService;
    private final UUID UserId = UUID.randomUUID();

    @Test
    @WithMockUser(roles = "MODERATOR")
    public void createUser_ShouldReturn200_WhenValidRequest() throws Exception {
        String validRequest = """
                        {
                            "username": "andrey2",
                            "email": "test@ya.com",
                "password": "1234",
                "lastName": "lastName",
                "firstName": "firstName"
                        }
                """;

        mvc.perform(post("/api/users")
                        .contentType("application/json")
                        .content(validRequest))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "MODERATOR")
    public void createUser_ShouldReturn400_WhenInValidEmail() throws Exception {
        String validRequest = """
                        {
                "username": "andreyBoyko",
                "email": "test",
                "password": "1234",
                "lastName": "lastName",
                "firstName": "firstName"
                        }
                """;

        mvc.perform(post("/api/users")
                        .contentType("application/json")
                        .content(validRequest))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "MODERATOR")
    public void createUser_ShouldReturn400_WhenEmptyLastName() throws Exception {
        String validRequest = """
                        {
                            "username": "andreyBoyko",
                            "email": "test",
                            "password": "1234",
                            "firstName": "firstName"
                        }
                """;

        mvc.perform(post("/api/users")
                        .contentType("application/json")
                        .content(validRequest))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "USER")
    public void createUser_ShouldReturn403_WhenNotModerator() throws Exception {
        String validRequest = """
                        {
                            "username": "andrey2",
                            "email": "test@ya.com",
                "password": "1234",
                "lastName": "lastName",
                "firstName": "firstName"
                        }
                """;

        mvc.perform(post("/api/users")
                        .contentType("application/json")
                        .content(validRequest))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "MODERATOR")
    public void getUserById_ShouldReturn200_WhenUserExists() throws Exception {

        UserResponse mockResponse = new UserResponse( "Andrey","Boyko", "test@example.com", List.of(),List.of());
        when(userService.getUserById(UserId)).thenReturn(mockResponse);

        mvc.perform(get("/api/users/{id}", UserId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.firstName").value("Andrey"))
                .andExpect(jsonPath("$.lastName").value("Boyko"));
    }

    @Test
    @WithMockUser(roles = "MODERATOR")
    public void getUserById_ShouldReturn404_WhenUserNotFound() throws Exception {
        when(userService.getUserById(any(UUID.class))).thenThrow(new UserNotFoundException());

        mvc.perform(get("/api/users/{id}", UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }
    @Test
    @WithMockUser(username = "hello_user", roles = "MODERATOR")
    public void hello_ShouldReturnAuthenticatedUsername() throws Exception {
        mvc.perform(get("/api/users/hello"))
                .andExpect(status().isOk())
                .andExpect(content().string("hello_user"));
    }
    @Test
    @WithMockUser(username = "hello_user", roles = "USER")
    public void hello_ShouldReturnAuthenticatedUsernameRole_USER() throws Exception {
        mvc.perform(get("/api/users/hello"))
                .andExpect(status().isForbidden())
                .andExpect(content().string(""));
    }

}
