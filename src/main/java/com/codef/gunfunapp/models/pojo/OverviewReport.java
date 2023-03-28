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
public class OverviewReport {

	private Double avgBarrel;
	private Double avgCost;
	private ArrayList<HashMap<String, String>> calibers;
	private ArrayList<HashMap<String, String>> frameMaterials;
	private ArrayList<HashMap<String, String>> manufacturers;
	private Double maxBarrel;
	private Double maxCost;
	private Double minBarrel;
	private Double minCost;
	private Long totalGuns;

}
