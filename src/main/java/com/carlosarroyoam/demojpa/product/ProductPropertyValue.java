package com.carlosarroyoam.demojpa.product;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Data;

@Entity
@Table(name = "product_property_values", uniqueConstraints = {
		@UniqueConstraint(name = "product_property_values_idx", columnNames = { "product_id", "property_id" }) })
@Data
public class ProductPropertyValue {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(length = 45, nullable = false)
	private String value;

	@JsonIgnore
	@ManyToOne
	@JoinColumn(name = "product_id", referencedColumnName = "id")
	private Product product;

	@ManyToOne
	@JoinColumn(name = "property_id", referencedColumnName = "id")
	private Property property;

}
