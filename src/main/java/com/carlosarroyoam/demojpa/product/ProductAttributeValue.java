package com.carlosarroyoam.demojpa.product;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "product_attribute_values", uniqueConstraints = {
		@UniqueConstraint(name = "product_attribute_values_idx", columnNames = { "product_id", "attribute_id" }) })
@Data
@NoArgsConstructor
public class ProductAttributeValue {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(length = 45, nullable = false)
	private String value;

	@JsonIgnore
	@OneToOne
	@JoinColumn(name = "product_id", referencedColumnName = "id")
	private Product product;

	@OneToOne
	@JoinColumn(name = "attribute_id", referencedColumnName = "id")
	private Attribute property;
}
