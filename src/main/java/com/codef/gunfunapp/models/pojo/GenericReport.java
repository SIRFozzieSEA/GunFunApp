package com.codef.gunfunapp.models.pojo;

import java.util.ArrayList;
import java.util.HashMap;

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
public class GenericReport {

	private String reportName;
	private String reportDetail;
	private ArrayList<HashMap<String, String>> reportLines;
	private HashMap<String, String> reportTotals;

	public void addToReportTotals(String key, String value) {
		this.reportTotals.put(key, value);
	}

}
