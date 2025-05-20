package com.example.demo;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.UUID;

@Entity
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "users")
public class User implements Serializable {
    @Id
    @GeneratedValue
    @EqualsAndHashCode.Include
    private UUID id;
    private String username;
}
