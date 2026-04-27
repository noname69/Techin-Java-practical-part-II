package lt.techin.taskmanager.service;

import lt.techin.taskmanager.dto.LoginRequest;
import lt.techin.taskmanager.dto.LoginResponse;
import lt.techin.taskmanager.dto.RegisterRequest;
import lt.techin.taskmanager.exception.DuplicateEmailException;
import lt.techin.taskmanager.exception.InvalidCredentialsException;
import lt.techin.taskmanager.model.Role;
import lt.techin.taskmanager.model.User;
import lt.techin.taskmanager.repository.UserRepository;
import lt.techin.taskmanager.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder encoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder encoder,
                       JwtService jwtService) {
        this.userRepository = userRepository;
        this.encoder = encoder;
        this.jwtService = jwtService;
    }

    public User register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateEmailException(request.email());
        }

        User user = new User();
        user.setName(request.name());
        user.setEmail(request.email());
        user.setPassword(encoder.encode(request.password()));
        user.setRole(Role.USER);

        return userRepository.save(user);
    }

    public LoginResponse login(LoginRequest request) {

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(InvalidCredentialsException::new);

        if (!encoder.matches(request.password(), user.getPassword())) {
            throw new InvalidCredentialsException();

        }

        String token = jwtService.generateToken(user);

        return new LoginResponse(
                "Bearer",
                token,
                user.getEmail(),
                List.of(user.getRole().name())
        );
    }
}
