package com.authentication.jwt.users.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.authentication.jwt.users.models.ERole;
import com.authentication.jwt.users.models.Role;

public interface RoleRepository extends MongoRepository<Role, String> {
  Optional<Role> findByName(ERole name);
}
