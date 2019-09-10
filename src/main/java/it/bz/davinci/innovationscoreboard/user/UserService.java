package it.bz.davinci.innovationscoreboard.user;

import it.bz.davinci.innovationscoreboard.user.dto.NewUserRequest;
import it.bz.davinci.innovationscoreboard.user.dto.ResetPasswordRequest;
import it.bz.davinci.innovationscoreboard.user.dto.UserResponse;
import it.bz.davinci.innovationscoreboard.user.jpa.UserRepository;
import it.bz.davinci.innovationscoreboard.user.jpa.UserRoleRepository;
import it.bz.davinci.innovationscoreboard.user.model.ApiUser;
import it.bz.davinci.innovationscoreboard.user.model.Role;
import it.bz.davinci.innovationscoreboard.user.model.UserRole;
import it.bz.davinci.innovationscoreboard.utils.rest.CollectionResponse;
import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.transaction.Transactional;
import javax.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Validated
@AllArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserResponse createUser(@Valid NewUserRequest newUser) {
        ApiUser apiUser = new ApiUser();
        apiUser.setEmail(newUser.getEmail().trim().toLowerCase());
        apiUser.setPassword(passwordEncoder.encode(newUser.getPassword()));
        apiUser.setEnabled(true);
        final ApiUser createdUser = userRepository.save(apiUser);

        UserRole userRole = new UserRole();
        userRole.setEmail(createdUser.getEmail());
        userRole.setRole(Role.ROLE_USER);

        userRoleRepository.save(userRole);

        return UserMapper.INSTANCE.to(createdUser);
    }

    public void resetPassword(String email, @Valid ResetPasswordRequest resetPasswordRequest) {
        final Optional<ApiUser> optionalApiUser = userRepository.findByEmail(email);

        final ApiUser user = optionalApiUser.orElseThrow(() -> new UsernameNotFoundException("User with email: " + email + " not found"));
        savePassword(resetPasswordRequest, user);
    }

    public void resetPassword(Integer id, @Valid ResetPasswordRequest resetPasswordRequest) {
        final Optional<ApiUser> optionalApiUser = userRepository.findById(id);

        final ApiUser user = optionalApiUser.orElseThrow(() -> new UsernameNotFoundException("User with id: " + id + " not found"));
        savePassword(resetPasswordRequest, user);
    }

    private void savePassword(@Valid ResetPasswordRequest resetPasswordRequest, ApiUser user) {
        user.setPassword(passwordEncoder.encode(resetPasswordRequest.getPassword()));

        userRepository.save(user);
    }

    public CollectionResponse<UserResponse> findAll() {
        final List<UserResponse> users = userRepository.findAll().stream()
                .map(UserMapper.INSTANCE::to)
                .collect(Collectors.toList());

        return new CollectionResponse<>(users);
    }

    @Transactional
    public void deleteUser(Integer id) {
        final ApiUser userToDelete = userRepository.getOne(id);
        userRoleRepository.deleteAllByEmail(userToDelete.getEmail());
        userRepository.delete(userToDelete);
    }
}
