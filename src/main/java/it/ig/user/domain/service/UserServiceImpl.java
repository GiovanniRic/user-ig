package it.ig.user.domain.service;

import it.ig.user.domain.mapper.UserMapper;
import it.ig.user.domain.model.dto.UserDTO;
import it.ig.user.domain.model.entity.RoleEntity;
import it.ig.user.domain.model.entity.UserEntity;
import it.ig.user.domain.repository.RoleRepository;
import it.ig.user.domain.repository.UserRepository;
import it.ig.user.exception.UserAppException;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;


    /**
     * Creates a new user.
     *
     * @param userDTO the user details
     * @return the created user
     * @throws UserAppException if the user already exists
     */
    @Override
    public UserDTO createUser(UserDTO userDTO) {

        try {
            var userEntity = userRepository.save(userMapper.toEntity(userDTO));
            log.info("Created user with email {}", userDTO.email());
            return userMapper.toDTO(userEntity);

        } catch (DataIntegrityViolationException ex) {
            log.error("Error creating user with email {}: {}", userDTO.email(), ex.getMessage(), ex);
            checkException(userDTO, ex);
            throw ex;
        }

    }


    /**
     * Updates an existing user.
     *
     * @param id      the user id
     * @param userDTO the user details
     * @throws UserAppException if the user does not exist
     */
    @Override
    @Transactional
    public void updateUser(Integer id, UserDTO userDTO) {

        var userEntity = getUserEntity(id);

        userEntity.setName(userDTO.name());
        userEntity.setSurname(userDTO.surname());
        userEntity.setUsername(userDTO.username());
        userEntity.setTaxCode(userDTO.taxCode());
        userRepository.save(userEntity);

        roleRepository.deleteByUserId(id);
        var newRolesUser = userDTO.roles().stream().map(role -> {
            var roleEntity = new RoleEntity();
            roleEntity.setUser(userEntity);
            roleEntity.setRole(role);
            return roleEntity;
        }).collect(Collectors.toSet());

        roleRepository.saveAll(newRolesUser);

        log.info("Updated user with id {}", id);

    }


    /**
     * Retrieves all users.
     *
     * @return a list of all users as UserDTOs
     */
    @Override
    public List<UserDTO> retrieveAllUsers() {
        return userRepository.findAll().stream().map(userMapper::toDTO).toList();
    }


    /**
     * Retrieves a user by id.
     *
     * @param id the user id
     * @return the user as a UserDTO
     * @throws EntityNotFoundException if the user does not exist
     */
    @Override
    public UserDTO retrieveUser(Integer id) {
        var userEntity = getUserEntity(id);
        return userMapper.toDTO(userEntity);
    }


/**
 * Deletes a user by id.
 *
 * @param id the id of the user to be deleted
 * @throws EntityNotFoundException if the user does not exist
 */
    @Override
    public void deleteUser(Integer id) {
        var user = getUserEntity(id);
        userRepository.delete(user);
    }

    private UserEntity getUserEntity(Integer id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(String.format("User with id %d not found", id)));
    }

    private static void checkException(UserDTO userDTO, DataIntegrityViolationException ex) {
        if (ex.getCause() instanceof ConstraintViolationException &&
                ex.getCause().getMessage().contains("Duplicate entry")) {
            throw new UserAppException(String.format("User with email %s already exists", userDTO.email()), HttpStatus.BAD_REQUEST);
        }
    }


}
