package com.ckt.api.user.service;

import com.ckt.api.base.PaginatedResponse;
import com.ckt.api.user.model.entity.User;
import com.ckt.api.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

    public PaginatedResponse<User> searchUsers(String search, int page, int perPage,
                                               String sortBy, String order) {
        Sort.Direction direction = order.equalsIgnoreCase("desc")
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        Pageable pageable = PageRequest.of(page - 1, perPage, Sort.by(direction, sortBy));
        Page<User> users = userRepo.searchUsers(search, pageable);

        return new PaginatedResponse<>(users);
    }
}
