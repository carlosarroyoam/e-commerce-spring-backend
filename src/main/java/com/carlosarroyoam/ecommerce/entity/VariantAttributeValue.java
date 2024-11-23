package com.carlosarroyoam.ecommerce.entity;

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
@Table(name = "variant_attribute_values", uniqueConstraints = {
		@UniqueConstraint(name = "variant_attribute_values_idx", columnNames = { "variant_id", "attribute_id" }) })
@Data
public class VariantAttributeValue {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "value", length = 45, nullable = false)
	private String value;

	@Column(name = "variant_id", nullable = false)
	private Long variantId;

	@JsonIgnore
	@ManyToOne
	@JoinColumn(name = "variant_id", referencedColumnName = "id", insertable = false, updatable = false, nullable = false)
	private Variant variant;

	@Column(name = "attribute_id", nullable = false)
	private Long attributeId;

	@ManyToOne
	@JoinColumn(name = "attribute_id", referencedColumnName = "id", insertable = false, updatable = false, nullable = false)
	private Attribute attribute;
}
