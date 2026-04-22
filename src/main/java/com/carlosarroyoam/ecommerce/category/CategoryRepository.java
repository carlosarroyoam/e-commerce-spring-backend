package com.carlosarroyoam.ecommerce.category;

import com.carlosarroyoam.ecommerce.category.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface CategoryRepository
    extends JpaRepository<Category, Byte>, JpaSpecificationExecutor<Category> {}
