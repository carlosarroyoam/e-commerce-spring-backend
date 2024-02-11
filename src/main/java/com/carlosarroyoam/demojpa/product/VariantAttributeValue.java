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

@Entity
@Table(name = "variant_attribute_values", uniqueConstraints = {
		@UniqueConstraint(name = "variant_attribute_values_idx", columnNames = { "variant_id", "attribute_id" }) })
@Data
public class VariantAttributeValue {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(length = 45, nullable = false)
	private String value;

	@JsonIgnore
	@OneToOne
	@JoinColumn(name = "variant_id", referencedColumnName = "id")
	private Variant variant;

	@OneToOne
	@JoinColumn(name = "attribute_id", referencedColumnName = "id")
	private Attribute attribute;

}
