package com.carlosarroyoam.ecommerce.category;

import com.carlosarroyoam.ecommerce.category.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/** Acceso a datos para la entidad {@link Category}. */
public interface CategoryRepository
    extends JpaRepository<Category, Byte>, JpaSpecificationExecutor<Category> {}
