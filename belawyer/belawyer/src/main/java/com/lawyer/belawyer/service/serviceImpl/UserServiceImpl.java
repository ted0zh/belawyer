package com.lawyer.belawyer.service.serviceImpl;

import com.lawyer.belawyer.data.dto.UserDto;
import com.lawyer.belawyer.data.entity.User;
import com.lawyer.belawyer.data.mapper.UserMapper;
import com.lawyer.belawyer.repository.UserRepository;
import com.lawyer.belawyer.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper=userMapper;
    }

    @Override
    public User createUser(UserDto dto) {
        User user = userMapper.toEntity(dto);
        return userRepository.save(user);
    }

    @Override
    public User updateUser(String username, UserDto dto){
        Optional<User> optionalUser = userRepository.findByUsername(username);
        if(optionalUser.isPresent()){
            User updatedUser = optionalUser.get();
            updatedUser.setEmail(dto.getEmail());
            updatedUser.setUsername(dto.getUsername());
            return userRepository.save(updatedUser);
        }else{
            return null;
        }
    }
    @Override
    public List<UserDto> fetchUsersDto() {
        List<User> allUsers = userRepository.findAll();
        //към DTO
        return allUsers.stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<User> getUser(Long id) {
        return userRepository.findById(id);
    }



    @Override
    public void deleteUser(String username) {
        User existing = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
        userRepository.delete(existing);
    }

    @Override
    public User findByUsername(String username) {
        return userRepository.findByUsername(username).get();
    }
}
