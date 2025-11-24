package com.codewithmosh.store.users;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor
@Getter
@Builder
public class UserDto {
    private Long id;
    private String firstName;
    private String lastName;
    private String username;
    private String email;

}
