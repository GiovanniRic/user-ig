package it.ig.user.domain.service;

import it.ig.user.domain.mapper.UserMapper;
import it.ig.user.domain.model.dto.UserDTO;
import it.ig.user.domain.model.entity.RoleEntity;
import it.ig.user.domain.model.entity.UserEntity;
import it.ig.user.domain.model.enums.Role;
import it.ig.user.domain.repository.RoleRepository;
import it.ig.user.domain.repository.UserRepository;
import it.ig.user.exception.UserAppException;
import jakarta.persistence.EntityNotFoundException;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;

import java.util.Arrays;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    private UserDTO userDTO;
    private UserEntity userEntity;

    @BeforeEach
    void setUp() {
        userDTO = new UserDTO(
                1,
                "Mario",
                "Rossi",
                "mario.rossi@example.com",
                "mario.rossi@example.com",
                "RSSMRA85M01H501Z",
                Set.of(Role.OPERATOR, Role.DEVELOPER)
        );

        userEntity = new UserEntity();
        userEntity.setId(1);
        userEntity.setName("Mario");
        userEntity.setSurname("Rossi");
        userEntity.setUsername("mario.rossi@example.com");
        userEntity.setEmail("mario.rossi@example.com");
        userEntity.setTaxCode("RSSMRA85M01H501Z");
        RoleEntity roleOperatorEntity = new RoleEntity(1, Role.OPERATOR, userEntity);
        RoleEntity roleDeveloperEntity = new RoleEntity(2, Role.DEVELOPER, userEntity);
        userEntity.setRoles(Set.of(roleOperatorEntity, roleDeveloperEntity));

    }

    @Test
    void createUserTestSuccess() {
        when(userMapper.toEntity(userDTO)).thenReturn(userEntity);
        when(userRepository.save(userEntity)).thenReturn(userEntity);
        when(userMapper.toDTO(userEntity)).thenReturn(userDTO);


        UserDTO result = userService.createUser(userDTO);

        assertNotNull(result);
        assertEquals(userDTO.email(), result.email());
        verify(userRepository).save(userEntity);
        verify(userMapper).toEntity(userDTO);
        verify(userMapper).toDTO(userEntity);
    }

    @Test
    void createUserTestDuplicatedEmail() {

        var constraintEx = mock(ConstraintViolationException.class);
        when(constraintEx.getMessage()).thenReturn(String.format("Duplicate entry '%s' for key 'email'", userDTO.email()));

        var dataIntegrityEx = new DataIntegrityViolationException("Duplicate entry", constraintEx);

        when(userMapper.toEntity(userDTO)).thenReturn(userEntity);
        when(userRepository.save(userEntity)).thenThrow(dataIntegrityEx);

        var exception = assertThrows(UserAppException.class,
                () -> userService.createUser(userDTO));

        assertEquals(String.format("User with email %s already exists", userDTO.email()), exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        verify(userRepository).save(userEntity);
    }


    @Test
    void updateUserTestSuccess() {

        var userId = 1;
        var updatedUserDTO = new UserDTO(
                userId,
                "Mario",
                "Bianchi",
                "mario.bianchi@example.com",
                "mario.bianchi@example.com",
                "BNHMRA85M01H501X",
                Set.of(Role.DEVELOPER, Role.MAINTAINER, Role.OPERATOR)
        );

        when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));
        userEntity.setName("Mario");
        userEntity.setSurname("Bianchi");
        userEntity.setUsername("mario.bianchi@example.com");
        userEntity.setTaxCode("BNHMRA85M01H501X");
        RoleEntity roleOperatorEntity = new RoleEntity(1, Role.OPERATOR, userEntity);
        RoleEntity roleDeveloperEntity = new RoleEntity(2, Role.DEVELOPER, userEntity);
        RoleEntity roleMaintainerEntity = new RoleEntity(3, Role.MAINTAINER, userEntity);
        var newRolesEntities = Set.of(roleOperatorEntity, roleDeveloperEntity, roleMaintainerEntity);
        userEntity.setRoles(newRolesEntities);
        when(userRepository.save(userEntity)).thenReturn(userEntity);

        userService.updateUser(userId, updatedUserDTO);

        verify(userRepository).findById(userId);
        verify(userRepository).save(userEntity);
        verify(roleRepository).deleteByUserId(userId);

        ArgumentCaptor<Set<RoleEntity>> roleCaptor = ArgumentCaptor.forClass(Set.class);
        verify(roleRepository).saveAll(roleCaptor.capture());

        var capturedRoles = roleCaptor.getValue();
        assertThat(capturedRoles).hasSize(3);
        capturedRoles.stream().map(RoleEntity::getRole)
                .forEach(role -> assertThat(updatedUserDTO.roles()).contains(role));
    }

    @Test
    void updateUserTestNotFound() {

        var userId = 1;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When & Then
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> userService.updateUser(userId, userDTO));

        assertEquals(String.format("User with id %s not found", userId), exception.getMessage());
        verify(userRepository).findById(userId);
        verify(userRepository, never()).save(any());
        verify(roleRepository, never()).deleteByUserId(any());
    }

    @Test
    void retrieveAllUsersTestSuccess() {

        var userEntities = Arrays.asList(userEntity);
        when(userRepository.findAll()).thenReturn(userEntities);
        when(userMapper.toDTO(userEntity)).thenReturn(userDTO);

        var results = userService.retrieveAllUsers();

        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals(userDTO, results.get(0));
        verify(userRepository).findAll();
        verify(userMapper).toDTO(userEntity);
    }

    @Test
    void retrieveAllUsersEmpty() {

        when(userRepository.findAll()).thenReturn(Arrays.asList());

        var results = userService.retrieveAllUsers();

        assertNotNull(results);
        assertTrue(results.isEmpty());
        verify(userRepository).findAll();
        verify(userMapper, never()).toDTO(any());
    }

    @Test
    void retrieveUserTestSuccess() {

        Integer userId = 1;
        when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));
        when(userMapper.toDTO(userEntity)).thenReturn(userDTO);

        UserDTO result = userService.retrieveUser(userId);

        assertNotNull(result);
        assertEquals(userDTO, result);
        verify(userRepository).findById(userId);
        verify(userMapper).toDTO(userEntity);
    }

    @Test
    void retrieveUserTestNotFound() {

        var userId = 1;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> userService.retrieveUser(userId));

        assertEquals(String.format("User with id %s not found", userId), exception.getMessage());
        verify(userRepository).findById(userId);
        verify(userMapper, never()).toDTO(any());
    }

    @Test
    void deleteUserTestSuccess() {

        var userId = 1;
        when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));

        userService.deleteUser(userId);

        verify(userRepository).findById(userId);
        verify(userRepository).delete(userEntity);
    }

    @Test
    void deleteUserTestNotFound() {

        var userId = 1;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> userService.deleteUser(userId));

        assertEquals(String.format("User with id %s not found", userId), exception.getMessage());
        verify(userRepository).findById(userId);
        verify(userRepository, never()).delete(any());
    }


}