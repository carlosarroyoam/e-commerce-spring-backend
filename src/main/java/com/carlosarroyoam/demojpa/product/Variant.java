package com.carlosarroyoam.demojpa.product;

import java.math.BigDecimal;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

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
@Table(name = "variants", uniqueConstraints = { @UniqueConstraint(name = "variants_sku_idx", columnNames = "sku") })
@Data
public class Variant {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(length = 64, nullable = false)
	private String sku;

	@Column(nullable = false)
	private BigDecimal price;

	@Column(nullable = false)
	private BigDecimal comparedAtPrice;

	@Column(nullable = false)
	private BigDecimal costPerItem;

	@Column(nullable = false)
	private Integer quantityOnStock;

	@JsonIgnore
	@ManyToOne()
	@JoinColumn(name = "product_id", referencedColumnName = "id")
	private Product product;

	@OneToMany(mappedBy = "variant", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	List<VariantAttributeValue> attributes;

	@OneToMany(mappedBy = "variant")
	private List<VariantImage> images;

}
