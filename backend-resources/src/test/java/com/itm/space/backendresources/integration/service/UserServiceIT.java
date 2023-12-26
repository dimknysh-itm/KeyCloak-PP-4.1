package com.itm.space.backendresources.integration.service;

import com.itm.space.backendresources.BaseIntegrationTest;
import com.itm.space.backendresources.api.request.UserRequest;
import com.itm.space.backendresources.api.response.UserResponse;
import com.itm.space.backendresources.mapper.UserMapper;
import com.itm.space.backendresources.service.UserService;
import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UserServiceIT extends BaseIntegrationTest {

    private final Keycloak keycloakClient;
    private final UserMapper userMapper;

    @Value("${keycloak.realm}")
    private String realm;
    @Autowired
    UserService userService;
    final UserRequest userRequest = new UserRequest("ivan", "test@mail.com", "test", "Ivan", "Ivanov");

    @Autowired
    UserServiceIT(Keycloak keycloakClient, UserMapper userMapper) {
        this.keycloakClient = keycloakClient;
        this.userMapper = userMapper;
    }

    @Test
    void createUser() {
        UserRequest user = new UserRequest("petya", "petr@mail.com", "test", "Petr", "petrov");
        String userId = userService.createUser(user);
        UserRepresentation userRepresentation = keycloakClient.realm(realm)
                .users().get(userId).toRepresentation();

        assertEquals(user.getUsername(), userRepresentation.getUsername());
        assertEquals(user.getEmail(), userRepresentation.getEmail());
        assertEquals(user.getFirstName(), userRepresentation.getFirstName());
        assertEquals(user.getLastName(), userRepresentation.getLastName());

        userService.deleteUserByUsername(user.getUsername());
    }


    @Test
    void getUserById() {
        UUID userId1 = UUID.fromString(userService.createUser(userRequest));

        UserResponse userResponse = userService.getUserById(userId1);
        UserRepresentation userRepresentation = keycloakClient.realm(realm)
                .users().get(String.valueOf(userId1)).toRepresentation();

        assertEquals(userRequest.getFirstName(), userResponse.getFirstName());
        assertEquals(userRequest.getLastName(), userResponse.getLastName());
        assertEquals(userRequest.getEmail(), userResponse.getEmail());
        assertEquals(userRequest.getUsername(), userRepresentation.getUsername());

        userService.deleteUserByUsername(userRequest.getUsername());

    }

}