package com.carlosarroyoam.demojpa.product;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "attributes", uniqueConstraints = {
		@UniqueConstraint(name = "attributes_title_idx", columnNames = "title") })
@Data
@NoArgsConstructor
public class Attribute {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(length = 45, nullable = false)
	private String title;

	@Column()
	private LocalDateTime deleted_at;
}
