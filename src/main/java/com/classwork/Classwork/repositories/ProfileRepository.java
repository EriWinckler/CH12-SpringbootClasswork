package com.classwork.Classwork.repositories;

import com.classwork.Classwork.models.Profile;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProfileRepository extends JpaRepository<Profile, Long> {
  List<Profile> findAllByFirstName(String firstName, Sort sort);

  List<Profile> findAllByLastName(String lastName, Sort sort);

  List<Profile> findAllByAddress(String address, Sort sort);

  Profile findByLastName(String lastName);

  Profile findByFirstName(String firstName);
}
