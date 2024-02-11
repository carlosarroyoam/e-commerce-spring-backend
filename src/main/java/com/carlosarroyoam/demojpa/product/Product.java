package com.carlosarroyoam.demojpa.product;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
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

	@Column(length = 96, nullable = false)
	private String title;

	@Column(length = 96, nullable = false)
	private String slug;

	@Column()
	private String description;

	@Column(columnDefinition = "TINYINT", nullable = false)
	private boolean featured;

	@Column(columnDefinition = "TINYINT", nullable = false)
	private boolean active;

	@ManyToOne()
	@JoinColumn(name = "category_id")
	private Category category;

	@OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private List<ProductAttributeValue> properties;

	@OneToMany(mappedBy = "product")
	private List<Variant> variants;

	@Column()
	private LocalDateTime createdAt;

	@Column()
	private LocalDateTime updatedAt;

	@Column()
	private LocalDateTime deletedAt;

}
