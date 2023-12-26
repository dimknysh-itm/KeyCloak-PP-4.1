package com.itm.space.backendresources.service;

import com.itm.space.backendresources.api.request.UserRequest;
import com.itm.space.backendresources.api.response.UserResponse;

import java.util.UUID;

public interface UserService {

    String createUser(UserRequest userRequest);

    UserResponse getUserById(UUID id);

    UUID getUserIdByUsername(String username);

    void deleteUserByUsername(String username);

}
