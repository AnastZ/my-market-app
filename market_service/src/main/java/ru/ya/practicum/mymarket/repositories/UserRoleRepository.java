package ru.ya.practicum.mymarket.repositories;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import ru.ya.practicum.mymarket.model.UserRole;

public interface UserRoleRepository extends ReactiveCrudRepository<UserRole, Long> {

    Flux<UserRole> findByUsername(String username);
}
