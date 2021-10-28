package com.switchfully.eurder.api.mappers;

import com.switchfully.eurder.api.dto.CreateUserDTO;
import com.switchfully.eurder.api.dto.UserDTO;
import com.switchfully.eurder.domain.entities.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserDTO toDTO(User user) {
        return new UserDTO.UserDTOBuilder()
                .withId(user.getId())
                .withFirstName(user.getFirstName())
                .withLastName(user.getLastName())
                .withEmail(user.getEmail())
                .withPhoneNumber(user.getPhoneNumber())
                .withAddress(user.getAddress())
                .withRole(user.getRole())
                .build();
    }

    public User toEntity(CreateUserDTO userDTO) {
        return User.createUser(userDTO.getFirstName(), userDTO.getLastName(), userDTO.getEmail(), userDTO.getAddress(), userDTO.getPhoneNumber());
    }

    public User toAdminEntity(CreateUserDTO userDTO) {
        return User.createAdmin(userDTO.getFirstName(), userDTO.getLastName(), userDTO.getEmail(), userDTO.getAddress(), userDTO.getPhoneNumber());
    }
}
