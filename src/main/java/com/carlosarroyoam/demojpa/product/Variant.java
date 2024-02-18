package com.carlosarroyoam.demojpa.product;

import java.math.BigDecimal;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

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
@Table(name = "variants", uniqueConstraints = { @UniqueConstraint(name = "variants_sku_idx", columnNames = "sku") })
@Data
public class Variant {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "sku", length = 64, nullable = false)
	private String sku;

	@Column(name = "price", nullable = false)
	private BigDecimal price;

	@Column(name = "compared_at_price", nullable = false)
	private BigDecimal comparedAtPrice;

	@Column(name = "cost_per_item", nullable = false)
	private BigDecimal costPerItem;

	@Column(name = "quantity_on_stock", nullable = false)
	private Integer quantityOnStock;

	@JsonIgnore
	@ManyToOne
	@JoinColumn(name = "product_id", referencedColumnName = "id")
	private Product product;

	@OneToMany(mappedBy = "variant")
	List<VariantAttributeValue> attributes;

	@OneToMany(mappedBy = "variant")
	private List<VariantImage> images;

}
