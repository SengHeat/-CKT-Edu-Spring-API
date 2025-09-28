package com.api.user.service;

import com.api.user.model.entity.User;
import com.api.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final UserRepository userRepo;

    public UserService(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    public Page<User> list(int page, int size) {
        return userRepo.findAll(PageRequest.of(page, size));
    }

    public User find(Long id) {
        return userRepo.findById(id).orElseThrow();
    }

    public User save(User u) { return userRepo.save(u); }

    public void delete(Long id) { userRepo.deleteById(id); }
}
