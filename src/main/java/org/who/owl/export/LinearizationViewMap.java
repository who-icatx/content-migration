package org.who.owl.export;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class LinearizationViewMap {

	public static final String LINEARIZATION_VIEW_MMS = "http://id.who.int/icd/release/11/mms";
	public static final String LINEARIZATION_VIEW_PCL = "http://id.who.int/icd/release/11/pcl";
	public static final String LINEARIZATION_VIEW_PCH = "http://id.who.int/icd/release/11/pch";
	public static final String LINEARIZATION_VIEW_DER = "http://id.who.int/icd/release/11/der";
	public static final String LINEARIZATION_VIEW_MNH = "http://id.who.int/icd/release/11/mnh";
	public static final String LINEARIZATION_VIEW_PED = "http://id.who.int/icd/release/11/ped";
	public static final String LINEARIZATION_VIEW_OCU = "http://id.who.int/icd/release/11/ocu";
	public static final String LINEARIZATION_VIEW_RAR = "http://id.who.int/icd/release/11/rar";
	public static final String LINEARIZATION_VIEW_NER = "http://id.who.int/icd/release/11/ner";
	public static final String LINEARIZATION_VIEW_MUS = "http://id.who.int/icd/release/11/mus";
	public static final String LINEARIZATION_VIEW_OPH = "http://id.who.int/icd/release/11/oph";
	public static final String LINEARIZATION_VIEW_ENV = "http://id.who.int/icd/release/11/env";
	public static final String LINEARIZATION_VIEW_ICD_O = "http://id.who.int/icd/release/11/icd-o";
	public static final String LINEARIZATION_VIEW_RESEARCH = "http://id.who.int/icd/release/11/research";
	public static final String LINEARIZATION_VIEW_ICF = "http://id.who.int/icd/release/11/icf";
	public static final String LINEARIZATION_VIEW_ICHI = "http://id.who.int/icd/release/11/ichi";

	public static final Set<String> TELESCOPIC_LINEARIZATIONS = new HashSet<String>() {{
		add(LINEARIZATION_VIEW_PCL);
		add(LINEARIZATION_VIEW_PCH);
		add(LINEARIZATION_VIEW_DER);
		add(LINEARIZATION_VIEW_MNH);
		add(LINEARIZATION_VIEW_PED);
		add(LINEARIZATION_VIEW_OCU);
		add(LINEARIZATION_VIEW_RAR);
		add(LINEARIZATION_VIEW_NER);
		add(LINEARIZATION_VIEW_MUS);
		add(LINEARIZATION_VIEW_OPH);
		add(LINEARIZATION_VIEW_ENV);
		add(LINEARIZATION_VIEW_RESEARCH);
	}};
	
	public static final Map<String, String> oldLinViewToNewLinViewMap = new HashMap<String, String>() {{
		
		put("http://who.int/icd#Morbidity", LINEARIZATION_VIEW_MMS); //01
		put("http://who.int/icd#Primary_Care_Low_RS", LINEARIZATION_VIEW_PCL); //03
		put("http://who.int/icd#Primary_Care_High_RS", LINEARIZATION_VIEW_PCH); //80.2
		put("http://who.int/icd#Specialty_Adaptation_Dermatology", LINEARIZATION_VIEW_DER); //06
		put("http://who.int/icd#Speciality_Adaptation_Mental_Health", LINEARIZATION_VIEW_MNH); //05
		put("http://who.int/icd#Specialty_Adaptation_Paediatrics", LINEARIZATION_VIEW_PED); //09
		put("http://who.int/icd#Specialty_Adaptation_Occupational_Health", LINEARIZATION_VIEW_OCU); //10
		put("http://who.int/icd#Specialty_Adaptation_Rare_Diseases", LINEARIZATION_VIEW_RAR); //12
		put("http://who.int/icd#Specialty_Adaptation_Neurology", LINEARIZATION_VIEW_NER); //08
		put("http://who.int/icd#Specialty_Adaptation_Musculoskeletal", LINEARIZATION_VIEW_MUS); //07
		put("http://who.int/icd#Specialty_Adaptation_Ophthalmology", LINEARIZATION_VIEW_OPH); //13
		put("http://who.int/icd#Specialty_Adaptation_Environmental_Health", LINEARIZATION_VIEW_ENV); //11
		put("http://who.int/icd#ICD-O", LINEARIZATION_VIEW_ICD_O); //25
		put("http://who.int/icd#Research", LINEARIZATION_VIEW_RESEARCH); //04
		put("http://who.int/icd#ICFLinearizationView", LINEARIZATION_VIEW_ICF); //02.02
		put("http://who.int/icd#ICHILinearizationView", LINEARIZATION_VIEW_ICHI); //02.01
	}};
	
	
	public static String getNewLinearizationViewName(String oldLinViewName) {
		return oldLinViewToNewLinViewMap.get(oldLinViewName);
	}
	
	public static boolean isTelescopicLinearization(String linView) {
		return TELESCOPIC_LINEARIZATIONS.contains(linView);
	}
}
