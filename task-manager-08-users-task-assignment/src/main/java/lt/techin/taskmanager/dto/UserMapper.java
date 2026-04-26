package lt.techin.taskmanager.dto;

import lt.techin.taskmanager.model.Project;
import lt.techin.taskmanager.model.User;

import java.util.List;

public class UserMapper {

    public static User toUser(CreateUserRequest request) {
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        return user;
    }

    public static User toUser(UpdateUserRequest request, Long id) {
        User user = new User();
        user.setId(user.getId());
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        return user;
    }

    public static UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail()
        );
    }

    public static List<UserResponse> toResponseList(List<User> users) {
        return users.stream()
                .map(UserMapper::toResponse)
                .toList();
    }
}
