package com.lawyer.belawyer.controller;

import com.lawyer.belawyer.data.dto.UserDto;
import com.lawyer.belawyer.data.entity.User;
import com.lawyer.belawyer.service.serviceImpl.UserServiceImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1/user")
public class UserController {
    private final UserServiceImpl userService;


    public UserController(UserServiceImpl userService) {
        this.userService = userService;
    }

    @RequestMapping("/create")
    public ResponseEntity<User> create(@RequestBody UserDto dto){
        return ResponseEntity.ok(userService.createUser(dto));
    }

    @RequestMapping("/update")
    public ResponseEntity<User> update(@RequestBody UserDto dto, @RequestParam Long id){
        return ResponseEntity.ok(userService.updateUser(id,dto));
    }

    @RequestMapping("/delete")
    public ResponseEntity<?> delete(@RequestParam Long id){
        Optional<User> userOpt = userService.getUser(id);
        if(userOpt.isPresent()){
            userService.deleteUser(id);
            return new ResponseEntity<>(HttpStatus.ACCEPTED);
        }else{
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
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
}
