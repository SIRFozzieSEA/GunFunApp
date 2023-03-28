package com.codef.gunfunapp.repos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.codef.gunfunapp.models.entities.ValidCaliber;

@Repository
public interface ValidCaliberRepo extends JpaRepository<ValidCaliber, Long> {


}

