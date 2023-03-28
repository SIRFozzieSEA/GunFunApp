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
@Table(name = "ammunition")
public class Ammunition {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ammo_pk")
	private long ammoPk;

	@Column(name = "caliber", length = 25)
	private String caliber;

	@Column(name = "no_of_rounds")
	private long noOfRounds;

	@Column(name = "purchase_date")
	private java.sql.Date purchaseDate;

	@Column(name = "purchase_cost", precision = 10, scale = 2, columnDefinition = "DECIMAL(10,2)")
	private BigDecimal purchaseCost;

}
