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
@Table(name = "variant_images", uniqueConstraints = {
		@UniqueConstraint(name = "variant_images_url_idx", columnNames = "url") })
@Data
public class VariantImage {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(length = 128, nullable = false)
	private String url;

	@JsonIgnore
	@ManyToOne
	@JoinColumn(name = "variant_id", referencedColumnName = "id")
	private Variant variant;

}
