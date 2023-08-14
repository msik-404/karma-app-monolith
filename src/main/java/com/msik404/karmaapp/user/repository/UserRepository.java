package com.msik404.karmaapp.user.repository;

import java.util.Optional;

import com.msik404.karmaapp.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;

public interface UserRepository extends JpaRepository<User, Long>, UserRepositoryCustom {

    Optional<User> findByUsername(@NonNull String username);

}
