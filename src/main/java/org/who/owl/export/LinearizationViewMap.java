package org.who.owl.export;

import java.util.HashMap;
import java.util.Map;

public class LinearizationViewMap {

	public static final Map<String, String> oldLinViewToNewLinViewMap = new HashMap<String, String>() {{
		
		put("http://who.int/icd#Morbidity", "http://id.who.int/icd/release/11/mms"); //01
		put("http://who.int/icd#Primary_Care_Low_RS", "http://id.who.int/icd/release/11/pcl"); //03
		put("http://who.int/icd#Primary_Care_High_RS", "http://id.who.int/icd/release/11/pch"); //80.2
		put("http://who.int/icd#Specialty_Adaptation_Dermatology", "http://id.who.int/icd/release/11/der"); //06
		put("http://who.int/icd#Speciality_Adaptation_Mental_Health","http://id.who.int/icd/release/11/mnh"); //05
		put("http://who.int/icd#Specialty_Adaptation_Paediatrics","http://id.who.int/icd/release/11/ped"); //09
		put("http://who.int/icd#Specialty_Adaptation_Occupational_Health","http://id.who.int/icd/release/11/ocu"); //10
		put("http://who.int/icd#Specialty_Adaptation_Rare_Diseases","http://id.who.int/icd/release/11/rar"); //12
		put("http://who.int/icd#Specialty_Adaptation_Neurology","http://id.who.int/icd/release/11/ner"); //08
		put("http://who.int/icd#Specialty_Adaptation_Musculoskeletal","http://id.who.int/icd/release/11/mus"); //07
		put("http://who.int/icd#Specialty_Adaptation_Ophthalmology","http://id.who.int/icd/release/11/oph"); //13
		put("http://who.int/icd#Specialty_Adaptation_Environmental_Health", "http://id.who.int/icd/release/11/env"); //11
		put("http://who.int/icd#ICD-O", "http://id.who.int/icd/release/11/icd-o"); //25
		put("http://who.int/icd#Research", "http://id.who.int/icd/release/11/research"); //04
		put("http://who.int/icd#ICFLinearizationView", "http://id.who.int/icd/release/11/icf"); //02.02
		put("http://who.int/icd#ICHILinearizationView", "http://id.who.int/icd/release/11/ichi"); //02.01
	}};
	
	
	public static String getNewLinearizationViewName(String oldLinViewName) {
		return oldLinViewToNewLinViewMap.get(oldLinViewName);
	}
}
