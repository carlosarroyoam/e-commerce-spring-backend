package com.carlosarroyoam.demojpa.product;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Data;

@Entity
@Table(name = "categories", uniqueConstraints = {
		@UniqueConstraint(name = "categories_title_idx", columnNames = "title") })
@Data
public class Category {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Byte id;

	@Column(name = "title", length = 45, nullable = false)
	private String title;

	@JsonIgnore
	@OneToMany(mappedBy = "category")
	private List<Product> products;

	@Column(name = "deleted_at")
	private LocalDateTime deletedAt;

}
