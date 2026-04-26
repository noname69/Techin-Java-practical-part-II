package lt.techin.taskmanager.service;

import lt.techin.taskmanager.exception.*;
import lt.techin.taskmanager.model.Project;
import lt.techin.taskmanager.model.Task;
import lt.techin.taskmanager.model.TaskStatus;
import lt.techin.taskmanager.model.User;
import lt.techin.taskmanager.repository.ProjectRepository;
import lt.techin.taskmanager.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> getAll() {
        return userRepository.findAll();
    }

    public User getById(Long id) {
        Optional<User> userOpt = userRepository.findById(id);

        if (userOpt.isPresent()) {
            return userOpt.get();
        }

        throw new UserNotFoundException(id);
    }

    public User create(User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new DuplicateEmailException(user.getEmail());
        }

        return userRepository.save(user);
    }

    public User update(Long id, User user) {
        User existing = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        if(userRepository.existsByEmailAndIdNot(user.getEmail(), id)) {
            throw new DuplicateEmailException(user.getEmail());
        }

        existing.setName(user.getName());
        existing.setEmail(user.getEmail());

        return userRepository.save(existing);
    }

    public void delete(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        userRepository.delete(user);
    }

}
