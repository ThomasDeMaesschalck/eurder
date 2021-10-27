package com.switchfully.eurder.api.dto;

import com.switchfully.eurder.domain.entities.User;

import java.util.UUID;

public class UserDTO {

    private final UUID id;
    private final String firstName;
    private final String lastName;
    private final String email;
    private final String address;
    private final String phoneNumber;
    private final User.Role role;

    public UserDTO(UUID id, String firstName, String lastName, String email, String address, String phoneNumber, User.Role role) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.role = role;
    }

    public UUID getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public String getAddress() {
        return address;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public User.Role getRole() {
        return role;
    }
}