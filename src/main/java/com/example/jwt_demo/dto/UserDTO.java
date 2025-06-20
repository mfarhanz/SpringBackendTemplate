package com.example.jwt_demo.dto;

import java.time.LocalDateTime;

import com.example.jwt_demo.model.User;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
public class UserDTO {
	private Long id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private LocalDateTime dateCreated;
    private LocalDateTime lastLogin;

    public UserDTO(User user) {
        this.setId(user.getId());
        this.setUsername(user.getUsername());
        this.setEmail(user.getEmail());
        this.setFirstName(user.getFirstName());
        this.setLastName(user.getLastName());
        this.setDateCreated(user.getDateCreated());
        this.setLastLogin(user.getLastLogin());
    }
}
