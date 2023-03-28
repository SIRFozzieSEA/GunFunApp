package com.codef.gunfunapp.models.entities;

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
@Table(name = "valid_calibers")
public class ValidCaliber {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "valid_caliber_pk")
	private long validCaliberPk;

	@Column(name = "caliber", length = 25)
	private String caliber;

	@Column(name = "shoots_caliber", length = 50)
	private String shootsCaliber;

}
