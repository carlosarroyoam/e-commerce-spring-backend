package com.carlosarroyoam.demojpa.product;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Data;

@Entity
@Table(name = "products", uniqueConstraints = { @UniqueConstraint(name = "products_slug_idx", columnNames = "slug") })
@Data
public class Product {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "title", length = 96, nullable = false)
	private String title;

	@Column(name = "slug", length = 96, nullable = false)
	private String slug;

	@Column(name = "description")
	private String description;

	@Column(name = "featured", columnDefinition = "BIT", nullable = false)
	private boolean featured;

	@Column(name = "active", columnDefinition = "BIT", nullable = false)
	private boolean active;

	@ManyToOne
	@JoinColumn(name = "category_id", referencedColumnName = "id", nullable = false)
	private Category category;

	@OneToMany(mappedBy = "product")
	private List<ProductPropertyValue> properties;

	@OneToMany(mappedBy = "product")
	private List<Variant> variants;

	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt;

	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;

	@Column(name = "deleted_at")
	private LocalDateTime deletedAt;

}
