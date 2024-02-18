package com.carlosarroyoam.demojpa.product;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Data;

@Data
public class ProductDTO {

	private Long id;
	private String title;
	private String slug;
	private String description;
	private boolean featured;
	private boolean active;
	private String category;
	private List<ProductPropertyValue> properties;
	private List<Variant> variants;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
	private LocalDateTime deletedAt;

}
