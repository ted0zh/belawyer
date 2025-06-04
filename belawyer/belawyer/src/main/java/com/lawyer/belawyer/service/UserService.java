package com.lawyer.belawyer.service;

import com.lawyer.belawyer.data.dto.UserDto;
import com.lawyer.belawyer.data.entity.User;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public interface UserService {
    User createUser(UserDto dto);
    User updateUser(String username, UserDto dto);
    List<UserDto> fetchUsersDto();
    Optional<User> getUser(Long id);
    void deleteUser(String name);
    User findByUsername(String username);

}
