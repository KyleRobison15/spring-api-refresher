package com.codewithmosh.store.users;

import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "name", expression = "java(user.getFirstName() + \" \" + user.getLastName())")
    UserDto toDto(User user);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "firstName", source = "name")
    @Mapping(target = "lastName", constant = "")
    @Mapping(target = "password", source = "password")
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "addresses", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "favoriteProducts", ignore = true)
    User toEntity(RegisterUserRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "firstName", source = "name")
    @Mapping(target = "lastName", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "addresses", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "favoriteProducts", ignore = true)
    void update(UpdateUserRequest request, @MappingTarget User user);
}
