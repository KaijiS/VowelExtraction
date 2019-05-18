package com.kaijis.VowelExtraction.model.Repository;

import com.kaijis.VowelExtraction.model.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsersRepository extends JpaRepository<Users, Long> {

    Optional<Users> findById(Long id);
    Optional<Users> findByName(String name);
    Optional<Users> findByOauthId(Long oauth_id);
    Optional<Users> findByToken(String token);
    Optional<Users> findByOneTimeToken(String onetimetoken);

}
