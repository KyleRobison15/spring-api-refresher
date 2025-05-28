package com.codewithmosh.store.users;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor
@Getter
@Builder
public class UserDto {
    private Long id;
    private String name;
    private String email;

}
