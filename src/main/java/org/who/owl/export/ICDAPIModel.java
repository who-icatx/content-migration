package org.who.owl.export;


import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

public class ICDAPIModel {
	
	private OWLOntologyManager manager;
	private OWLDataFactory df;
    private OWLOntology targetOnt;
	
    private OWLAnnotationProperty releaseDate;
    private OWLAnnotationProperty releaseId;
    
	private OWLAnnotationProperty titleProp;
	private OWLAnnotationProperty defProp;
	private OWLAnnotationProperty longDefProp;
	
	private OWLAnnotationProperty labelProp;
	
	private OWLAnnotationProperty noteProp;
	private OWLAnnotationProperty codingHintProp;
	
	private OWLAnnotationProperty fullNameProp;
	private OWLAnnotationProperty exclusionProp;
	private OWLAnnotationProperty inclusionProp;
	private OWLAnnotationProperty browserUrlProp;
	private OWLAnnotationProperty foundRefProp;
	private OWLAnnotationProperty isObsoleteProp;
	
	private OWLAnnotationProperty icd10codeProp;
	private OWLAnnotationProperty baseIndexProp;
	private OWLAnnotationProperty indexTypeProp;
	private OWLAnnotationProperty isInclusionProp;
	
	private OWLAnnotationProperty subclsBaseInclusionProp;
	private OWLAnnotationProperty referencedEntityProp;
	private OWLAnnotationProperty baseExclusionProp;
	
	private OWLClass termCls;
	private OWLClass languageTermCls;
	private OWLClass baseIndexTermCls;
	private OWLClass baseExclusionTermCls;
	
	private OWLNamedIndividual narrowerIndexTypeInst;
	private OWLNamedIndividual synonymIndexTypeInst;
	
	
	public ICDAPIModel(OWLOntologyManager manager, OWLOntology targetOnt) {
		this.manager = manager;
		this.targetOnt = targetOnt;
		this.df = manager.getOWLDataFactory();
		initModel();
	}

	private void initModel() {
		releaseDate = getAnnotationProperty(ICDAPIConstants.RELEASE_DATE);
		releaseId = getAnnotationProperty(ICDAPIConstants.RELEASE_ID);
		
		titleProp = getAnnotationProperty(ICDAPIConstants.TITLE);
		defProp = getAnnotationProperty(ICDAPIConstants.DEFINITION);
		longDefProp = getAnnotationProperty(ICDAPIConstants.LONG_DEFINITION);

		labelProp = getAnnotationProperty(ICDAPIConstants.LABEL);
		
		fullNameProp = getAnnotationProperty(ICDAPIConstants.FULLY_SPECIFIED_NAME);
		exclusionProp = getAnnotationProperty(ICDAPIConstants.EXCLUSION);
		inclusionProp = getAnnotationProperty(ICDAPIConstants.INCLUSION);
		browserUrlProp = getAnnotationProperty(ICDAPIConstants.BROWSER_URL);
		foundRefProp = getAnnotationProperty(ICDAPIConstants.FOUNDATION_REFERENCE);
		
		noteProp = getAnnotationProperty(ICDAPIConstants.NOTE);
		codingHintProp = getAnnotationProperty(ICDAPIConstants.CODING_HINT);
		
		isObsoleteProp = getAnnotationProperty(ICDAPIConstants.IS_OBSOLETE);
		icd10codeProp = getAnnotationProperty(ICDAPIConstants.ICD10CODE);
		
		termCls = getCls(ICDAPIConstants.TERM_CLS);
		languageTermCls = getCls(ICDAPIConstants.LANGUAGE_TERM_CLS);
		baseIndexTermCls = getCls(ICDAPIConstants.BASE_INDEX_TERM_CLS);
		baseExclusionTermCls = getCls(ICDAPIConstants.BASE_EXCLUSION_TERM_CLS);
		
		narrowerIndexTypeInst = getOWLIndividual(ICDAPIConstants.NARROWER_INST);
		synonymIndexTypeInst = getOWLIndividual(ICDAPIConstants.SYNONYM_INST);
		indexTypeProp = getAnnotationProperty(ICDAPIConstants.INDEX_TYPE);
		baseIndexProp = getAnnotationProperty(ICDAPIConstants.BASE_INDEX_PROP);
		isInclusionProp = getAnnotationProperty(ICDAPIConstants.IS_INCLUSION);
		
		subclsBaseInclusionProp = getAnnotationProperty(ICDAPIConstants.SUBCLASS_BASE_INCLUSION_PROP);
		referencedEntityProp = getAnnotationProperty(ICDAPIConstants.REFERENCED_ENTITY_PROP);
		baseExclusionProp = getAnnotationProperty(ICDAPIConstants.BASE_EXCLUSION_PROP);
	}
	
	private OWLAnnotationProperty getAnnotationProperty(String propIRI) {
		OWLAnnotationProperty p = df.getOWLAnnotationProperty(propIRI);
		//manager.addAxiom(targetOnt, df.getOWLDeclarationAxiom(p)); //TODO: check if it exists already?
		return p;
	}

	public OWLOntology getTargetOnt() {
		return targetOnt;
	}

	public OWLAnnotationProperty getTitleProp() {
		return titleProp;
	}

	public OWLAnnotationProperty getDefProp() {
		return defProp;
	}

	public OWLAnnotationProperty getLongDefProp() {
		return longDefProp;
	}

	public OWLAnnotationProperty getLabelProp() {
		return labelProp;
	}
	
	public OWLAnnotationProperty getNoteProp() {
		return noteProp;
	}
	
	public OWLAnnotationProperty getCodingHintProp() {
		return codingHintProp;
	}

	public OWLAnnotationProperty getFullNameProp() {
		return fullNameProp;
	}

	public OWLAnnotationProperty getExclusionProp() {
		return exclusionProp;
	}

	public OWLAnnotationProperty getInclusionProp() {
		return inclusionProp;
	}

	public OWLAnnotationProperty getBrowserUrlProp() {
		return browserUrlProp;
	}

	public OWLAnnotationProperty getFoundRefProp() {
		return foundRefProp;
	}
	
	public OWLAnnotationProperty getIsObsoleteProp() {
		return isObsoleteProp;
	}
	
	public OWLAnnotationProperty getICD10CodeProp() {
		return icd10codeProp;
	}
	
	public OWLAnnotationProperty getReleaseDate() {
		return releaseDate;
	}
	
	public OWLAnnotationProperty getReleaseId() {
		return releaseId;
	}
	
	public OWLObjectProperty getPostCoordinationProp(String sourceProp) {
		//TODO: Clarify if the postcoordination properties will keep the same IRI, or will use a different one
		String targetName = sourceProp.replace(ICDAPIConstants.SOURCE_ONT_NS, ICDAPIConstants.TARGET_POSTCOORDINATION_NS);
		return df.getOWLObjectProperty(targetName);
	}
	
	private OWLClass getCls(String cls) {
		return df.getOWLClass(cls);
	}
	
	public OWLClass getTermCls() {
		return termCls;
	}
	
	public OWLClass getLanguageTermCls() {
		return languageTermCls;
	}
	
	public OWLClass getBaseIndexCls() {
		return baseIndexTermCls;
	}
	
	public OWLClass getBaseExclusionCls() {
		return baseExclusionTermCls;
	}
	
	private OWLNamedIndividual getOWLIndividual(String inst) {
		return df.getOWLNamedIndividual(inst);
	}
	
	public OWLNamedIndividual getNarrowerIndexTypeInst() {
		return narrowerIndexTypeInst;
	}
	
	public OWLNamedIndividual getSynonymIndexTypeInst() {
		return synonymIndexTypeInst;
	}
	
	public OWLAnnotationProperty getIndexTypeProp() {
		return indexTypeProp;
	}
	
	public OWLAnnotationProperty getBaseIndexProp() {
		return baseIndexProp;
	}
	
	public OWLAnnotationProperty isInclusionProp() {
		return isInclusionProp;
	}
	
	public OWLAnnotationProperty getSubclsBaseInclusionProp() {
		return subclsBaseInclusionProp;
	}
 
	public OWLAnnotationProperty getReferencedEntityProp() {
		return referencedEntityProp;
	}
	
	public OWLAnnotationProperty getBaseExcusionProp() {
		return baseExclusionProp;
	}
}
