package com.example.bs.service;

import com.example.bs.models.User;
import com.example.bs.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final RoleService roleService;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, RoleService roleService ,BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.userRepository = userRepository;
        this.roleService = roleService;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    @Override
    public User findByUsername(String username) { //обертка над методом репозитория, что бы не обращаться напрямую
        return userRepository.findByUsername(username);
    }


    @Transactional(readOnly = true)
    @Override
    public List<User> readAllUsers() {
        return userRepository.findAll();
    }

    @Transactional(readOnly = true)
    @Override
    public User readUserById(Long id) {
        Optional<User> user = userRepository.findById(id);
        return user.orElseThrow(() -> new EntityNotFoundException("User not found"));
    }

    @Transactional
    @Override
    public void saveUser(User user) {
        if (user.getAuthorities() == null || user.getAuthorities().isEmpty()) {
            user.setAuthorities(roleService.findByUsername("ROLE_USER"));
        }
        user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        userRepository.save(user);
    }

    @Transactional
    @Override
    public void updateUser(Long id, User user) {
        try {
            User user1 = readUserById(id);
            user.setUsername(user.getUsername());
            user1.setEmail(user.getEmail());
            user1.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
            user1.setAuthorities(user.getAuthorities());
            userRepository.save(user1);
        } catch (NullPointerException e) {
            throw new EntityNotFoundException();
        }
    }

    @Transactional
    @Override
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
}