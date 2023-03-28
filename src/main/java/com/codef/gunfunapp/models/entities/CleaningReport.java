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
@Table(name = "cleaning_reports")
public class CleaningReport {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "report_clean_pk")
	private long reportCleanPk;

	@Column(name = "nickname", length = 20)
	private String nickname;

	@Column(name = "caliber", length = 25)
	private String caliber;

	@Column(name = "no_of_rounds")
	private long noOfRounds;

	@Column(name = "last_fired_date")
	private java.sql.Date lastFiredDate;
	
	@Column(name = "last_cleaned_date")
	private java.sql.Date lastDateCleaned;

}
