package com.switchfully.eurder.services;

import com.switchfully.eurder.api.dto.CreateUserDTO;
import com.switchfully.eurder.api.dto.UserDTO;
import com.switchfully.eurder.api.mappers.UserMapper;
import com.switchfully.eurder.domain.entities.User;
import com.switchfully.eurder.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserMapper userMapper;
    private final UserRepository userRepository;
    private boolean adminExists;

    @Autowired
    public UserService(UserMapper userMapper, UserRepository userRepository) {
        this.userMapper = userMapper;
        this.userRepository = userRepository;
    }

    public UserDTO saveUser(CreateUserDTO userDTO) {
        User created = userRepository.save(userMapper.toEntity(userDTO));
        return userMapper.toDTO(created);
    }

    public UserDTO saveAdmin(CreateUserDTO adminToCreate) {
        if(adminExists)
        {
            throw new IllegalArgumentException("There can be only one admin");
        }
        User created = userRepository.save(userMapper.toAdminEntity(adminToCreate));
        adminExists = true;
        return userMapper.toDTO(created);
    }

    public List<UserDTO> getAllUsers(UUID adminId) {
        assertAdminId(adminId);

        return userRepository.getUsers().stream()
                .sorted(Comparator.comparing(User::getLastName).thenComparing(User::getFirstName))
                .map(userMapper::toDTO)
                .collect(Collectors.toList());
    }

    public void assertAdminId(UUID adminId) {
        User user = userRepository.getById(adminId);
        if (user.getRole() != User.Role.ADMIN) {
            throw new IllegalArgumentException("Unauthorized user");
        }
    }

}