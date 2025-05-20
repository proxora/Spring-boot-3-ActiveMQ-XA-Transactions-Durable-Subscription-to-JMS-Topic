package com.example.demo;


import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserChangedEvent {
    User user;
}