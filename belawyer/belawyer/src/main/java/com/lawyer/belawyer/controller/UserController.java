package com.lawyer.belawyer.controller;

import com.lawyer.belawyer.data.dto.UserDto;
import com.lawyer.belawyer.data.entity.User;
import com.lawyer.belawyer.service.serviceImpl.UserServiceImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/user")
public class UserController {
    private final UserServiceImpl userService;


    public UserController(UserServiceImpl userService) {
        this.userService = userService;
    }
    @PreAuthorize("hasRole('ADMIN')")
    @RequestMapping("/create")
    public ResponseEntity<User> create(@RequestBody UserDto dto){
        return ResponseEntity.ok(userService.createUser(dto));
    }
    @PreAuthorize("hasRole('ADMIN')")
    @RequestMapping("/update")
    public ResponseEntity<User> update(@RequestBody UserDto dto, @RequestParam String username){
        return ResponseEntity.ok(userService.updateUser(username,dto));
    }
//
@PreAuthorize("hasRole('ADMIN')")
@DeleteMapping("/delete/{username}")
public ResponseEntity<Void> delete(@PathVariable String username) {
    try {
        userService.deleteUser(username);
        return ResponseEntity.noContent().build();
    } catch (RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
}

    @RequestMapping("/get")
    public ResponseEntity<User> get(@RequestParam Long id){
        Optional<User> userOpt = userService.getUser(id);
        if(userOpt.isPresent()){
            userService.getUser(id);
            return new ResponseEntity<>(HttpStatus.FOUND);
        }else{
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
    }
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/fetch")
    public ResponseEntity<List<UserDto>> fetchAllUsers() {
        List<UserDto> all = userService.fetchUsersDto();
        return ResponseEntity.ok(all);
    }
}
