package com.classwork.Classwork.controllers;

import com.classwork.Classwork.models.Profile;
import com.classwork.Classwork.repositories.ProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {
  @Autowired
  private ProfileRepository repository;

  @GetMapping
  public @ResponseBody
  List<Profile> getProfiles() { return repository.findAll(); }

  @GetMapping("/{id}")
  public @ResponseBody Profile getProfileById(@PathVariable Long id) {
    return repository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
  }

  @GetMapping("/address/{address}")
  public ResponseEntity<List<Profile>> getAllProfilesByAddress(@PathVariable String address) {
    return new ResponseEntity<>(repository.findAllByAddress(address,
        Sort.by("firstName")), HttpStatus.OK);
  }

  @PostMapping
  public ResponseEntity<Profile> createProfile(@RequestBody Profile newProfile) {
    return new ResponseEntity<>(repository.save(newProfile),
        HttpStatus.CREATED);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<String> deleteProfile(@PathVariable Long id) {
    repository.deleteById(id);
    return new ResponseEntity<>("Deleted", HttpStatus.OK);
  }
}
