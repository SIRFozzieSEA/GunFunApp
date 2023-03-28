package com.codef.gunfunapp.models.entities;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@Builder
@ToString(includeFieldNames = true)
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "ammunition_reports")
public class AmmunitionReport {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "report_ammo_pk")
	private long reportAmmoPk;

	@Column(name = "caliber", length = 25)
	private String caliber;

	@Column(name = "no_of_rounds")
	private long noOfRounds;

	@Column(name = "purchase_quantity")
	private long purchaseQuantity;

	@Column(name = "purchase_date")
	private java.sql.Date purchaseDate;

	@Column(name = "purchase_cost", precision = 10, scale = 2, columnDefinition = "DECIMAL(10,2)")
	private BigDecimal purchaseCost;

	@Column(name = "last_purchase_quantity")
	private long lastPurchaseQuantity;

	@Column(name = "last_purchase_date")
	private java.sql.Date lastPurchaseDate;

	@Column(name = "last_purchase_cost", precision = 10, scale = 2, columnDefinition = "DECIMAL(10,2)")
	private BigDecimal lastPurchaseCost;

	@Column(name = "fired_quantity")
	private long firedQuantity;

	@Column(name = "fired_date")
	private java.sql.Date firedDate;

	@Column(name = "last_fired_quantity")
	private long lastFiredQuantity;

	@Column(name = "last_fired_date")
	private java.sql.Date lastFiredDate;

}
