package com.example.oceanbase.demos.web.repository;


import com.example.oceanbase.demos.web.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserEntityRepository extends JpaRepository<User,String> {

    User findById(Integer id);

    User findByUsernameAndPassword(String username, String password);
}
