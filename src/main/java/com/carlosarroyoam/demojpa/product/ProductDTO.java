package com.carlosarroyoam.demojpa.product;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ProductDTO {
	private Long id;

	private String title;

	private String slug;

	private String description;

	private boolean featured;

	private boolean active;

	private String category;

	private List<ProductAttributeValue> properties;

	private List<Variant> variants;

	private LocalDateTime created_at;

	private LocalDateTime updated_at;

	private LocalDateTime deleted_at;
}
