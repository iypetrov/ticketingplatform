package org.example.usersservice.repositories;

import org.example.usersservice.entities.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserRepository extends JpaRepository<UserEntity, UUID>  {
}
