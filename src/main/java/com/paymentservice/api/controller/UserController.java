package com.paymentservice.api.controller;

import com.paymentservice.api.dtos.UserDTO;
import com.paymentservice.api.dtos.UserResponseDTO;
import com.paymentservice.api.dtos.UserUpdateDTO;
import com.paymentservice.api.model.User;
import com.paymentservice.api.services.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService){
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<UserResponseDTO>> getAllUsers(){
        return ResponseEntity.
                ok().
                body(userService.getAllAccounts()
                        .stream()
                        .map(UserResponseDTO::new)
                        .toList());
    }

    @PostMapping
    public ResponseEntity<UserResponseDTO> createNewUser(@RequestBody @Valid UserDTO userData){
        UserResponseDTO newUser = userService.addNewUser(userData);

        return ResponseEntity.
                status(HttpStatus.CREATED).
                body(newUser);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> updateUser(@RequestBody @Valid UserUpdateDTO newUserData,
                                                      @PathVariable Long id){
        userService.updateUser(id, newUserData);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id){
        userService.deleteUser(id);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
