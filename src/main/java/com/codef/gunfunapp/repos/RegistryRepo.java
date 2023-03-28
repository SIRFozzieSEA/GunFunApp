package com.codef.gunfunapp.repos;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.codef.gunfunapp.models.entities.Registry;

@Repository
public interface RegistryRepo extends JpaRepository<Registry, Long> {

	@Query("SELECT gr FROM Registry gr WHERE gr.nickname=:possibleNicknameValue")
	Optional<Registry> findByNickname(@Param("possibleNicknameValue") String possibleNicknameValue);

}

