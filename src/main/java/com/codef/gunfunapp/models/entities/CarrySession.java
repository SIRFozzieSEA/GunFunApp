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
@Table(name = "carry_sessions")
public class CarrySession {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "carry_pk")
	private long carryPk;

	@Column(name = "nickname", length = 20)
	private String nickname;

	@Column(name = "carried_date")
	private java.sql.Date carriedDate;

	@Column(name = "day_of_week", length = 10)
	private String dayOfWeek;
	
	public CarrySession(String nickname, java.sql.Date dateCarried, String dayOfWeek) {
		super();
		this.nickname = nickname;
		this.carriedDate = dateCarried;
		this.dayOfWeek = dayOfWeek;
	}

}
