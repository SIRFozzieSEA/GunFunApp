package com.codef.gunfunapp.repos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.codef.gunfunapp.models.entities.ShootingSession;

@Repository
public interface ShootingSessionRepo extends JpaRepository<ShootingSession, Long> {
}
