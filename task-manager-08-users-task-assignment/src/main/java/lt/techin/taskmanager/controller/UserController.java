package lt.techin.taskmanager.controller;

import jakarta.validation.Valid;
import lt.techin.taskmanager.dto.*;
import lt.techin.taskmanager.model.User;
import lt.techin.taskmanager.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    //    GET /api/users
    @GetMapping
    public List<UserResponse> getAll() {
        return UserMapper.toResponseList(userService.getAll());
    }

    //    GET /api/users/{id}
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(UserMapper.toResponse(userService.getById(id)));
    }

    //    POST /api/users
    @PostMapping
    public ResponseEntity<UserResponse> create(
            @RequestBody @Valid CreateUserRequest request) {

        User user = userService.create(UserMapper.toUser(request));

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(UserMapper.toResponse(user));
    }

    //    PUT /api/users/{id}
    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> update(
            @PathVariable Long id,
            @RequestBody @Valid UpdateUserRequest request
    ) {

        User user = UserMapper.toUser(request, id);

        return ResponseEntity.ok(
                UserMapper.toResponse(
                        userService.update(id, user)
                )
        );
    }

    //    DELETE /api/users/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }

}
