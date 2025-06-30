package com.itm.space.backendresources;

import com.itm.space.backendresources.helpclasses.annotation.WithMockOAuth2User;
import com.itm.space.backendresources.api.response.UserResponse;
import com.itm.space.backendresources.helpclasses.UserNotFoundException;
import com.itm.space.backendresources.service.UserService;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import org.springframework.security.test.context.support.WithMockUser;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.UUID;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class IntegrationTest extends BaseIntegrationTest {
    @MockBean
    private UserService userService;
    private final UUID UserId = UUID.randomUUID();

    //Модератор успешно добавляет пользователя

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
    // неверный емайл, будет бедреквест
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
// Если один из параметров пустой, то запись не производится
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
// USER не может добавлять пользователей
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
// Возврат существующего пользователя
    @Test
    @WithMockUser(roles = "MODERATOR")
    public void getUserById_ShouldReturn200_WhenUserExists() throws Exception {

        UserResponse mockResponse = new UserResponse( "Andrey","Boyko", "test@example.com", List.of(),List.of());
        when(userService.getUserById(UserId)).thenReturn(mockResponse);

        mvc.perform(get("/api/users/{id}", UserId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.firstName").value("Andrey"))
                .andExpect(jsonPath("$.lastName").value("Boyko"))
                .andExpect(jsonPath("$.roles").isArray())
                .andExpect(jsonPath("$.groups").isEmpty());
    }
//Юзер не найден
    @Test
    @WithMockUser(roles = "MODERATOR")
    public void getUserById_ShouldReturn404_WhenUserNotFound() throws Exception {

        when(userService.getUserById(UserId)).thenThrow(UserNotFoundException.class);
        mvc.perform(get("/api/users/{id}", UserId))
                .andExpect(status().isNotFound());
    }

    // Возвращает имя аутентифицированного пользователя MODERATOR
    @Test
    @WithMockUser(username = "hello_user", roles = "MODERATOR")
    public void hello_ShouldReturnAuthenticatedUsername() throws Exception {
        mvc.perform(get("/api/users/hello"))
                .andExpect(status().isOk())
                .andExpect(content().string("hello_user"));
    }
    // Не возвращает имя аутентифицированного пользователя c ролью USER
    @Test
    @WithMockUser(username = "hello_user", roles = "USER")
    public void hello_ShouldReturnAuthenticatedUsernameRole_USER() throws Exception {
        mvc.perform(get("/api/users/hello"))
                .andExpect(status().isForbidden())
                .andExpect(content().string(""));
    }

    //авторизованный MODERATOR должен мочь добавлять пользователей
    @Test
    @WithMockOAuth2User(authorities = "ROLE_MODERATOR")
    public void createUser_WithOAuth2_ShouldReturn200() throws Exception {
        mvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                        {
                                            "username": "andrey2",
                                            "email": "test@ya.com",
                                "password": "1234",
                                "lastName": "lastName",
                                "firstName": "firstName"
                                        }
                                """))
                .andExpect(status().isOk());
    }
    //авторизованный USER не должен мочь добавлять пользователей
    @Test
    @WithMockOAuth2User(authorities = "ROLE_USER")
    public void createUser_WithOAuth2_ShouldReturn403() throws Exception {
        mvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                        {
                                            "username": "andrey2",
                                            "email": "test@ya.com",
                                "password": "1234",
                                "lastName": "lastName",
                                "firstName": "firstName"
                                        }
                                """))
                .andExpect(status().isForbidden());
    }

}
