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
@Table(name = "cleaning_sessions")
public class CleaningSession {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "clean_pk")
	private long cleanPk;

	@Column(name = "nickname", length = 20)
	private String nickname;

	@Column(name = "cleaned_date")
	private java.sql.Date cleanedDate;
	
	public CleaningSession(String nickname, java.sql.Date dateCleaned) {
		super();
		this.nickname = nickname;
		this.cleanedDate = dateCleaned;
	}

}
