package lt.techin.taskmanager.controller;

import jakarta.validation.Valid;
import lt.techin.taskmanager.dto.CreateUserRequest;
import lt.techin.taskmanager.dto.UpdateUserRequest;
import lt.techin.taskmanager.dto.UserMapper;
import lt.techin.taskmanager.dto.UserResponse;
import lt.techin.taskmanager.model.User;
import lt.techin.taskmanager.service.DefaultUserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final DefaultUserService defaultUserService;

    public UserController(DefaultUserService defaultUserService) {
        this.defaultUserService = defaultUserService;
    }

    //    GET /api/users
    @GetMapping
    public List<UserResponse> getAll() {
        return UserMapper.toResponseList(defaultUserService.getAll());
    }

    //    GET /api/users/{id}
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(UserMapper.toResponse(defaultUserService.getById(id)));
    }

    //    POST /api/users
    @PostMapping
    public ResponseEntity<UserResponse> create(
            @RequestBody @Valid CreateUserRequest request) {

        User user = defaultUserService.create(UserMapper.toUser(request));

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
                        defaultUserService.update(id, user)
                )
        );
    }

    //    DELETE /api/users/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        defaultUserService.delete(id);
        return ResponseEntity.noContent().build();
    }

}
