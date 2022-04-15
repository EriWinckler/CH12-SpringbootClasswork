package com.classwork.Classwork.controllers;

import com.classwork.Classwork.models.auth.ERole;
import com.classwork.Classwork.models.auth.Role;
import com.classwork.Classwork.models.auth.User;
import com.classwork.Classwork.payloads.request.LoginRequest;
import com.classwork.Classwork.payloads.request.SignupRequest;
import com.classwork.Classwork.payloads.response.JwtResponse;
import com.classwork.Classwork.payloads.response.MessageResponse;
import com.classwork.Classwork.repositories.RoleRepository;
import com.classwork.Classwork.repositories.UserRepository;
import com.classwork.Classwork.security.jwt.JwtUtils;
import com.classwork.Classwork.security.services.UserDetailsImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@CrossOrigin
@RestController
@RequestMapping("api/auth")
public class AuthController {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    private JwtUtils jwtUtils;

    @Value("${spring.datasource.driver-class-name}")
    public String myDriver;

    @Value("${spring.datasource.url}")
    public String myUrl;

    @Value("${spring.datasource.username}")
    public String username;

    @Value("${spring.datasource.password}")
    public String password;

    @PostMapping("/signin")
    public ResponseEntity<JwtResponse> authenticateUser (@RequestBody LoginRequest loginRequest ) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());
        return ResponseEntity.ok(new JwtResponse(jwt,
                userDetails.getId(),
                userDetails.getUsername(),
                roles));
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@RequestBody SignupRequest signupRequest) {
        if(userRepository.existsByUsername(signupRequest.getUsername())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Email already in use please login or reset password"));
        }

        User user = new User(signupRequest.getUsername(), encoder.encode(signupRequest.getPassword()));
        Set<String> strRoles = signupRequest.getRoles();
        Set<Role> roles = new HashSet<>();

        int roleCheck = roleRepository.isRoleEmpty();

        if (roleCheck < ERole.values().length) {
            int id = 1;
            for (ERole role : ERole.values()) {
                if (roleRepository.findByName(role).isEmpty()) {
                    try {
                        Connection conn = DriverManager.getConnection(myUrl, username, password);
                        Class.forName(myDriver);
                        String query = "Insert into role (id, name) values (?,?)";
                        PreparedStatement statement = conn.prepareStatement(query);

                        statement.setString(1, Integer.toString(id));
                        statement.setString(2, role.toString());

                        statement.executeUpdate();

                    } catch (Exception e) {
                        Logger logger = LoggerFactory.getLogger(AuthController.class);
                        System.out.println(e.getMessage());

                    }
                }
                id++;
            }
        }

        if (strRoles == null) {
            Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                    .orElseThrow(() -> new RuntimeException("Error: Role is not found"));
            roles.add(userRole);
        } else {
            strRoles.forEach(role -> {
                switch(role) {
                    case "admin":
                    case "administrator":
                        Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found"));
                        roles.add(adminRole);

                        break;

                    default:
                        Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found"));
                        roles.add(userRole);

                        break;
                }
            });
        }
        user.setRoles(roles);
        userRepository.save(user);

        return new ResponseEntity(new MessageResponse("User created successfully"), HttpStatus.CREATED);
    }
}
