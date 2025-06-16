package it.ig.user.domain.service;

import it.ig.user.domain.model.dto.UserDTO;

import java.util.List;

public interface UserService {

    UserDTO createUser(UserDTO userDTO);
    void updateUser(Integer id, UserDTO userDTO);

    List<UserDTO> retrieveAllUsers();
    UserDTO retrieveUser(Integer id);

    void deleteUser(Integer id);
}
