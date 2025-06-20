package com.example.jwt_demo.model;

import jakarta.persistence.*;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
//import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import org.hibernate.annotations.CreationTimestamp;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true)
    private String username;
    
    @Column(unique = true, nullable = true)
    private String email;  // Add an email field for recovery
    
    private String password;
    private String firstName;
    private String lastName;
    
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private Set<Role> roles = new HashSet<>();
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)		// new
    private Set<Collection> collections = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)		// new
    private Set<Token> tokens = new HashSet<>();
    
    @Column(nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime dateCreated;
    
    private LocalDateTime lastLogin;
    private boolean emailVerified = false;
    
    public User(String username, String email, String password, Set<Role> roles) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.roles = roles;
    }
    
    @Override
    public String toString() {
    	return "{\n" +
	           "  \"id\": " + id + ",\n" +
	           "  \"username\": \"" + username + "\",\n" +
	           "  \"email\": \"" + email + "\",\n" +
	           "  \"password\": \"" + password + "\",\n" +
	           "  \"firstName\": \"" + firstName + "\",\n" +
	           "  \"lastName\": \"" + lastName + "\",\n" +
	           "  \"roles\": \"" + roles + "\",\n" +
	           "  \"emailVerified\": " + emailVerified + ",\n" +
	           "  \"dateCreated\": \"" + dateCreated.toString() + "\",\n" +
	           "  \"lastLogin\": \"" + lastLogin.toString() + "\"\n" +
	           "}";
    }
}
