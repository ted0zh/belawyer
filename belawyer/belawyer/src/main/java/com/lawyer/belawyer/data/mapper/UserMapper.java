package com.lawyer.belawyer.data.mapper;

import com.lawyer.belawyer.data.dto.CaseResponseDto;
import com.lawyer.belawyer.data.dto.UserDto;
import com.lawyer.belawyer.data.entity.Case;
import com.lawyer.belawyer.data.entity.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Component
public class UserMapper {

    private final PasswordEncoder passwordEncoder;

    public UserMapper(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    public UserDto toDto(User user){
        if (user == null) {
            return null;
        }

        return new UserDto(
                user.getUsername(),
                user.getEmail(),
                user.getPassword(),
                user.getRole()
        );
    }

    public User toEntity(UserDto dto){
        if (dto == null) {
            return null;
        }

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRole(dto.getRole());
        user.setCreatedAt(Timestamp.from(Instant.now()));

        return user;
    }
    public UserDto toResponseDto(User user) {
        if (user == null) {
            return null;
        }
        UserDto dto = new UserDto();
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole());
        dto.setUsername(user.getUsername());
        return dto;
    }
    public List<UserDto> toResponseDtoList(List<User> users) {
        if (users == null) {
            return null;
        }

        List<UserDto> list = new ArrayList<>(users.size());
        for (User user : users) {
            list.add(toResponseDto(user));
        }

        return list;
    }
}
