package org.who.owl.export;


public class ICDAPIConstants {

	public final static String TARGET_ONT_NAME = "http://who.int/icd";
	public static final String SOURCE_ONT_NS = "http://who.int/icd#";
	public static final String TARGET_ONT_NS = SOURCE_ONT_NS;
	
	public static final String TARGET_POSTCOORDINATION_NS = "http://id.who.int/icd/schema/";
	
	public static final String CM_ONT_NAME = "https://who.int/whofic/contentmodel";
	
	public final static String ERROR_CLS = "http://who.int/icd#Z_ErrorClasses";
	
	public static final String SOURCE_ICF_ENTITY_CLS = "http://who.int/icd#ICFEntity";
	public static final String SOURCE_ICD_ENTITY_CLS = "http://who.int/icd#ICDEntity";
	public static final String SOURCE_ICHI_ENTITY_CLS = "http://who.int/icd#ICHIEntity";
	
	public final static String RELEASE_DATE = TARGET_ONT_NS + "releaseDate";
	public final static String RELEASE_ID = TARGET_ONT_NS + "releaseId";
	public final static String RELEASE_ID_BETA = "beta";
	
	public final static String TITLE = TARGET_ONT_NS + "title";
	public final static String DEFINITION = TARGET_ONT_NS + "definition";
	public final static String LONG_DEFINITION = TARGET_ONT_NS + "longDefinition";
	//public final static String SYNONYM = TARGET_ONT_NS + "synonym";
	
	public final static String LABEL = TARGET_ONT_NS + "label";
	
	public final static String FULLY_SPECIFIED_NAME = TARGET_ONT_NS + "fullySpecifiedName";
	//public final static String NARROWER_TERM = TARGET_ONT_NS + "narrowerTerm";
	public final static String EXCLUSION = TARGET_ONT_NS + "exclusion";
	public final static String INCLUSION = TARGET_ONT_NS + "inclusion";
	
	public final static String BROWSER_URL = TARGET_ONT_NS + "browserUrl";
	public final static String FOUNDATION_REFERENCE = TARGET_ONT_NS + "foundationReference";
	
	public final static String REFERENCED_ENTITY_PROP = TARGET_ONT_NS + "referencedEntity";

	public final static String NARROWER_INST = TARGET_ONT_NS + "IndexType.Narrower";
	public final static String SYNONYM_INST = TARGET_ONT_NS + "IndexType.Synonym";
	public final static String INDEX_TYPE = TARGET_ONT_NS + "indexType";
	public final static String BASE_INDEX_PROP = TARGET_ONT_NS + "baseIndex";
	
	public final static String SUBCLASS_BASE_INCLUSION_PROP = TARGET_ONT_NS + "subclassBaseInclusion";
	
	public final static String NOTE = TARGET_ONT_NS + "note"; //ICD-10
	public final static String CODING_HINT = TARGET_ONT_NS + "codingHint"; //ICD-10
	public final static String CODING_NOTE = TARGET_ONT_NS + "codingNote"; //ICD-11 per linearization
	
	public final static String ICD10CODE = TARGET_ONT_NS + "icd10code";
	
	public final static String IS_OBSOLETE = TARGET_ONT_NS + "isObsolete";
	
	public final static String IS_INCLUSION = TARGET_ONT_NS + "isInclusion";
	
	public final static String BASE_EXCLUSION_PROP = TARGET_ONT_NS + "baseExclusion";
	
	public final static String ICF_REFERENCE_PROP = TARGET_ONT_NS + "icfReference";
	public final static String RELATED_IMPAIRMENT_PROP = TARGET_ONT_NS + "relatedImpairment";
	
	public final static String EN_LANG = "en";
	
	
	// ************************ CLASSES ************************** //
	
	public final static String TERM_CLS = TARGET_ONT_NS + "Term";
	public final static String LANGUAGE_TERM_CLS = TARGET_ONT_NS + "LanguageTerm";
	public final static String BASE_INDEX_TERM_CLS = TARGET_ONT_NS + "BaseIndexTerm";
	
	public final static String BASE_EXCLUSION_TERM_CLS = TARGET_ONT_NS + "BaseExclusionTerm";
	
}
