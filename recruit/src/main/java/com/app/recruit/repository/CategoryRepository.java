package com.app.recruit.repository;

import com.app.recruit.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    boolean existsByCategoryName(String categoryName);
    Optional<Category> findByCategoryName(String categoryName);
}
