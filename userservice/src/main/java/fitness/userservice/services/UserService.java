package fitness.userservice.services;

import org.springframework.stereotype.Service;

import fitness.userservice.dto.RegisterRequest;
import fitness.userservice.dto.UserResponse;
import fitness.userservice.model.User;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@AllArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository repository;

    public UserResponse register(RegisterRequest request) {

        if(repository.existsByEmail(request.getEmail())){
            User existingUser = repository.findByEmail(request.getEmail());
            if (existingUser.getKeycloakId() == null && request.getKeycloakId() != null) {
                existingUser.setKeycloakId(request.getKeycloakId());
                repository.save(existingUser);
            }
            UserResponse userResponse = new UserResponse();
            userResponse.setId(existingUser.getId());
            userResponse.setEmail(existingUser.getEmail());
            userResponse.setPassword(existingUser.getPassword());
            userResponse.setFirstName(existingUser.getFirstName());
            userResponse.setLastName(existingUser.getLastName());
            userResponse.setCreatedAt(existingUser.getCreatedAt());
            userResponse.setUpdatedAt(existingUser.getUpdatedAt());
            return userResponse;
        }
      
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword());
        user.setKeycloakId(request.getKeycloakId());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        
        User savedUser = repository.save(user);

        UserResponse userResponse = new UserResponse();
        userResponse.setId(savedUser.getId());
        userResponse.setEmail(savedUser.getEmail());
        userResponse.setPassword(savedUser.getPassword());
        userResponse.setKeycloakId(savedUser.getKeycloakId());
        userResponse.setFirstName(savedUser.getFirstName());
        userResponse.setLastName(savedUser.getLastName());
        userResponse.setCreatedAt(savedUser.getCreatedAt());
        userResponse.setUpdatedAt(savedUser.getUpdatedAt());
        return userResponse;
    }

    public UserResponse getUserProfile(String userId) {
       User user = repository.findById(userId).orElseThrow(()->new RuntimeException("User with id " + userId + " not found"));
       UserResponse userResponse = new UserResponse();
       userResponse.setId(user.getId());
       userResponse.setEmail(user.getEmail());
       userResponse.setPassword(user.getPassword());
       userResponse.setFirstName(user.getFirstName());
       userResponse.setLastName(user.getLastName());
       userResponse.setCreatedAt(user.getCreatedAt());
       userResponse.setUpdatedAt(user.getUpdatedAt());
       return userResponse;
    }


    public boolean existByUserId(String userId) {
        log.info("Calling user service for {}",userId);
        return repository.existsByKeycloakId(userId);
    }

}
