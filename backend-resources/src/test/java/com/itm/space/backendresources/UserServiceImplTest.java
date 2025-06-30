package com.itm.space.backendresources;

import com.itm.space.backendresources.api.request.UserRequest;
import com.itm.space.backendresources.api.response.UserResponse;
import com.itm.space.backendresources.exception.BackendResourcesException;
import com.itm.space.backendresources.mapper.UserMapper;
import com.itm.space.backendresources.service.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.*;
import org.keycloak.representations.idm.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private Keycloak keycloakClient;

    @Mock
    private UserMapper userMapper;

    @Mock
    private RealmResource realmResource;

    @Mock
    private UsersResource usersResource;

    @Mock
    private UserResource userResource;

    @Mock
    private RoleMappingResource roleMappingResource;

    @Mock
    private MappingsRepresentation mappingsRepresentation;

    @InjectMocks
    private UserServiceImpl userService;

    private final String testRealm = "ITM";
    private final String userId = UUID.randomUUID().toString();

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(userService, "realm", testRealm);
    }

    @Test
    void createUser_Success() throws Exception {
        // Given
        UserRequest request = new UserRequest(
                "testUser", "pass123", "test@email.com", "John", "Doe"
        );

        Response response = mock(Response.class);

        // Настройка цепочки вызовов Keycloak
        when(keycloakClient.realm(testRealm)).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.create(any(UserRepresentation.class))).thenReturn(response);

        // Правильная настройка response
        when(response.getStatusInfo()).thenReturn(Response.Status.CREATED);
//        when(response.getStatus()).thenReturn(201);
        when(response.getLocation()).thenReturn(new URI("/admin/realms/" + testRealm + "/users/" + userId));

        // When
        userService.createUser(request);

        // Then
        verify(usersResource).create(any(UserRepresentation.class));
    }

    @Test
    void createUser_KeycloakError_ThrowsException() {
        // Given
        UserRequest request = new UserRequest(
                "testUser", "pass123", "test@email.com", "John", "Doe"
        );

        Response errorResponse = mock(Response.class);
        when(errorResponse.getStatusInfo()).thenReturn(Response.Status.BAD_REQUEST);
        when(errorResponse.getStatus()).thenReturn(400);

        when(keycloakClient.realm(testRealm)).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.create(any(UserRepresentation.class))).thenReturn(errorResponse);

        // When & Then
        assertThrows(BackendResourcesException.class, () -> userService.createUser(request));
    }

    @Test
    void getUserById_Success() {
        // Given
        UserRepresentation userRep = new UserRepresentation();
        userRep.setUsername("testUser");
        userRep.setEmail("test@email.com");
        userRep.setFirstName("John");
        userRep.setLastName("Doe");

        List<RoleRepresentation> roles = List.of(new RoleRepresentation("role1", "Role 1", false));
        List<GroupRepresentation> groups = List.of(new GroupRepresentation());

        when(keycloakClient.realm(testRealm)).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.get(userId)).thenReturn(userResource);
        when(userResource.toRepresentation()).thenReturn(userRep);
        when(userResource.roles()).thenReturn(roleMappingResource);
        when(roleMappingResource.getAll()).thenReturn(mappingsRepresentation);
        when(mappingsRepresentation.getRealmMappings()).thenReturn(roles);
        when(userResource.groups()).thenReturn(groups);

        UserResponse expectedResponse = new UserResponse(
                "testUser",
                "test@email.com",
                "John Doe",
                List.of("role1"),
                List.of("group1")
        );

        when(userMapper.userRepresentationToUserResponse(userRep, roles, groups))
                .thenReturn(expectedResponse);

        // When
        UserResponse result = userService.getUserById(UUID.fromString(userId));

        // Then
        assertNotNull(result);
        assertEquals(expectedResponse.getFirstName(), result.getFirstName());
        assertEquals(expectedResponse.getEmail(), result.getEmail());
    }

    @Test
    void getUserById_KeycloakError_ThrowsException() {
        // Given
        when(keycloakClient.realm(testRealm)).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.get(userId)).thenThrow(new NotFoundException("User not found"));

        // When & Then
        assertThrows(BackendResourcesException.class,
                () -> userService.getUserById(UUID.fromString(userId)));
    }
}