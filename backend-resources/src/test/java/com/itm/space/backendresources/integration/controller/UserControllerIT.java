package com.itm.space.backendresources.integration.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itm.space.backendresources.BaseIntegrationTest;
import com.itm.space.backendresources.api.request.UserRequest;
import com.itm.space.backendresources.api.response.UserResponse;
import com.itm.space.backendresources.mapper.UserMapper;
import com.itm.space.backendresources.service.UserService;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import javax.ws.rs.core.Response;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;

@WithMockUser(username = "test", password = "test", authorities = "ROLE_MODERATOR")
class UserControllerIT extends BaseIntegrationTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    UserService userService;
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    Keycloak keycloakClient;
    @Value("${keycloak.realm}")
    private String realm;
    @Autowired
    UserMapper userMapper;
    final UserRequest userRequest = new UserRequest("ivan", "test@mail.com", "test", "Ivan", "Ivanov");


    @Test
    void createNewUser() throws Exception {

        String json = objectMapper.writeValueAsString(userRequest);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                )
                .andExpectAll(
                        status().is2xxSuccessful()
                );

        userService.deleteUserByUsername(userRequest.getUsername());
    }

    @SneakyThrows
    @Test
    void createNewUserIfUserAlreadyExist() {
        String json = objectMapper.writeValueAsString(userRequest);
        userService.createUser(userRequest);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                )
                .andExpect(status().is4xxClientError());

        userService.deleteUserByUsername(userRequest.getUsername());
    }

    @SneakyThrows
    @ParameterizedTest
    @MethodSource("getParametrsForCreateUser")
    void createNewUserWithBlankFields(String username, String email, String password, String firstName, String lastName) {
        UserRequest userRequest = new UserRequest(username, email, password, firstName, lastName);
        String json = objectMapper.writeValueAsString(userRequest);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().is4xxClientError());
    }

    public static Stream<Arguments> getParametrsForCreateUser() {
        return Stream.of(
                Arguments.of("", "test@mail.com", "test", "Ivan", "Ivanov"),
                Arguments.of("ivan", "", "test", "Ivan", "Ivanov"),
                Arguments.of("ivan", "test@mail.com", "", "Ivan", "Ivanov"),
                Arguments.of("ivan", "test@mail.com", "test", "", "Ivanov"),
                Arguments.of("ivan", "test@mail.com", "test", "Ivan", "")
        );
    }


    @Test
    @SneakyThrows
    void getUserById() {
        String userId = userService.createUser(userRequest);
        UserRepresentation userRepresentation = keycloakClient.realm(realm).users().get(String.valueOf(userId)).toRepresentation();
        List<RoleRepresentation> userRoles = keycloakClient.realm(realm)
                .users().get(String.valueOf(userId)).roles().getAll().getRealmMappings();
        List<GroupRepresentation> userGroups = keycloakClient.realm(realm).users().get(String.valueOf(userId)).groups();
        UserResponse userResponse = userMapper.userRepresentationToUserResponse(userRepresentation, userRoles, userGroups);
        String json = objectMapper.writeValueAsString(userResponse);

        mockMvc.perform(get("/api/users/" + userId))
                .andExpectAll(
                        status().isOk(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        content().json(json)
                );

        userService.deleteUserByUsername(userRequest.getUsername());
    }

    @Test
    @SneakyThrows
    void hello() {

        mockMvc.perform(get("/api/users/hello")
                        .with(httpBasic("test", "test")))
                .andExpect(status().isOk())
                .andExpect(content()
                        .string("test"));

    }
}