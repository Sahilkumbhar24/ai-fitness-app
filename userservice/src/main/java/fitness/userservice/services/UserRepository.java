package fitness.userservice.services;

import org.springframework.data.jpa.repository.JpaRepository;

import fitness.userservice.model.User;

public interface UserRepository extends JpaRepository<User, String> {

    boolean existsByEmail(String email);

    boolean existsByKeycloakId(String userId);

    User findByEmail(String email);

}
