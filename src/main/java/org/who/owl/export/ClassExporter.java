package org.who.owl.export;

import java.util.Collection;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;

import edu.stanford.bmir.whofic.icd.ICDContentModel;
import edu.stanford.smi.protegex.owl.model.RDFProperty;
import edu.stanford.smi.protegex.owl.model.RDFResource;
import edu.stanford.smi.protegex.owl.model.RDFSNamedClass;

public class ClassExporter {

	private OWLOntologyManager manager;
	private OWLDataFactory df;
    private OWLOntology targetOnt;
    
    private ICDContentModel cm;
    private ICDAPIModel icdapiModel;
    private LogicalDefinitionCreator logDefCreator;
    
    private RDFSNamedClass sourceCls;
    
    private boolean isICTM = false;
    

	public ClassExporter(RDFSNamedClass cls, OWLOntologyManager manager, 
			OWLOntology targetOnt, ICDContentModel cm, ICDAPIModel icdapiModel, boolean isICTM) {
		this.sourceCls = cls;
		this.manager = manager;
		this.df = manager.getOWLDataFactory();
		this.targetOnt = targetOnt;
		this.cm = cm;
		this.icdapiModel = icdapiModel;
		this.isICTM = isICTM;
		this.logDefCreator = new LogicalDefinitionCreator(cm, icdapiModel, manager, targetOnt);
	}
    
	public OWLClass export() {
		OWLClass cls = createCls(PublicIdCache.getPublicId(cm, sourceCls));
		
		addTitle(cls);
		addDefinition(cls);
		addLongDefinition(cls);
		
		addFullySpecifiedTitle(cls);
		addCodingHint(cls);
		addNote(cls);
		
		addPublicBrowserLink(cls);
		
		addSyns(cls);
		addNarrower(cls);
		addInclusions(cls);
		
		addExclusions(cls);
		
		addIsObosolte(cls);
		
		addLogicalDefinition(cls);
		
		return cls;
	}


	private void addExclusions(OWLClass cls) {
		
		Collection<RDFResource> terms = cm.getTerms(sourceCls, cm.getBaseExclusionProperty());
		for (RDFResource term : terms) {
			OWLNamedIndividual targetTerm = createBaseExclusionTerm(term);
			
			// add reference category
			RDFSNamedClass refCls = (RDFSNamedClass) term.getPropertyValue(cm.getReferencedCategoryProperty());
			
			if (refCls != null) {
				IRI refIri = IRI.create(PublicIdCache.getPublicId(cm, refCls));
					if (refIri != null) {
						OWLAnnotation ann = df.getOWLAnnotation(icdapiModel.getReferencedEntityProp(), refIri);
						OWLAnnotationAssertionAxiom annAssertAx = df.getOWLAnnotationAssertionAxiom(targetTerm.getIRI(), ann);
						manager.addAxiom(targetOnt, annAssertAx);
					}
			}
			
			//add alternative label
			String label = (String) term.getPropertyValue(cm.getLabelProperty());
			if (label != null) {
				OWLAnnotation ann = df.getOWLAnnotation(icdapiModel.getLabelProp(), df.getOWLLiteral(label, ICDAPIConstants.EN_LANG));
				manager.addAxiom(targetOnt, df.getOWLAnnotationAssertionAxiom(targetTerm.getIRI(), ann));
			}
			
			OWLAnnotation ann = df.getOWLAnnotation(icdapiModel.getBaseExcusionProp(), targetTerm.getIRI());
			manager.addAxiom(targetOnt,	 df.getOWLAnnotationAssertionAxiom(cls.getIRI(), ann));
		}
	}

	private OWLNamedIndividual createBaseExclusionTerm(RDFResource term) {
		OWLNamedIndividual targetTerm = df.getOWLNamedIndividual(term.getName());
		manager.addAxiom(targetOnt, df.getOWLClassAssertionAxiom(icdapiModel.getBaseExclusionCls(), targetTerm));
		return targetTerm;
	}
	
	private void addInclusions(OWLClass cls) {
		addIndexBaseInclusions(cls);
		addSubclassBaseInclusions(cls);
	}

	private void addSubclassBaseInclusions(OWLClass cls) {
		
		RDFProperty refCatProp = cm.getReferencedCategoryProperty();
		if (refCatProp == null) { //happens in ICTM
			return;
		}
		addReferenceAsIRIAnnotations(cls, icdapiModel.getSubclsBaseInclusionProp(), cm.getSubclassBaseInclusionProperty());
	}

	private void addNarrower(OWLClass cls) {
		addLanguageTermAnnotations(cls, icdapiModel.getBaseIndexProp(), cm.getNarrowerProperty(), icdapiModel.getNarrowerIndexTypeInst());
	}

	private void addSyns(OWLClass cls) {
		addLanguageTermAnnotations(cls, icdapiModel.getBaseIndexProp(), cm.getSynonymProperty(), icdapiModel.getSynonymIndexTypeInst());
	}

	// index terms that are neither syns nor narrower terms, just inclusions
	private void addIndexBaseInclusions(OWLClass cls) {
		addLanguageTermAnnotations(cls, icdapiModel.getBaseIndexProp(), cm.getSynonymProperty(), null);
	}

	private void addPublicBrowserLink(OWLClass cls) {
		OWLAnnotation ann = df.getOWLAnnotation(icdapiModel.getBrowserUrlProp(), IRI.create(StringUtils.getSimplePublicBrowserLink(cls.getIRI().toString())));
		manager.addAxiom(targetOnt, df.getOWLAnnotationAssertionAxiom(cls.getIRI(), ann));
	}

	private void addNote(OWLClass cls) {
		addLanguageTermAnnotations(cls, icdapiModel.getNoteProp(), cm.getNoteProperty());
	}

	private void addCodingHint(OWLClass cls) {
		addLanguageTermAnnotations(cls, icdapiModel.getCodingHintProp(), cm.getCodingHintProperty());
	}

	private void addFullySpecifiedTitle(OWLClass cls) {
		addLanguageTermAnnotations(cls, icdapiModel.getFullNameProp(), cm.getFullySpecifiedNameProperty());
	}

	private void addTitle(OWLClass cls) {
		addLanguageTermAnnotations(cls, icdapiModel.getTitleProp(), cm.getIcdTitleProperty());
	}
	
	private void addDefinition(OWLClass cls) {
		addLanguageTermAnnotations(cls, icdapiModel.getDefProp(), cm.getDefinitionProperty());
	}

	private void addLongDefinition(OWLClass cls) {
		addLanguageTermAnnotations(cls, icdapiModel.getLongDefProp(), cm.getLongDefinitionProperty());
	}
	
	
	private void addIsObosolte(OWLClass cls) {
		RDFProperty isObsoleteProp = cm.getIsObsoleteProperty();
		Object isObsoleteObj = sourceCls.getPropertyValue(isObsoleteProp);
		if (isObsoleteObj != null && isObsoleteObj instanceof Boolean) {
			boolean isObsolete = (Boolean) isObsoleteObj;
			addBooleanAnnotation(cls, icdapiModel.getIsObsoleteProp(), isObsolete);
			if (isObsolete == true) {
				deprecateCls(cls);
			}
		}
	}
	

	private void addLogicalDefinition(OWLClass cls) {
		logDefCreator.createLogicalAxioms(sourceCls, cls);
	}

	
	/******************* Generic methods ********************/
	
	private OWLClass createCls(String iri) {
		OWLClass cls = df.getOWLClass(iri);
		manager.addAxiom(targetOnt, df.getOWLDeclarationAxiom(cls));
		return cls;
	}
 
	
	private void addReferenceAsIRIAnnotations(OWLClass cls, OWLAnnotationProperty targetProp, RDFProperty sourceProp) {
		Collection<RDFResource> terms = cm.getTerms(sourceCls, sourceProp);
		for (RDFResource term : terms) {
			addReferenceAsIRIAnnotationFromTerm(cls, targetProp, sourceProp, term);
		}
	}

	private void addReferenceAsIRIAnnotationFromTerm(OWLClass cls, OWLAnnotationProperty targetProp, RDFProperty sourceProp,
			RDFResource term) {
		RDFProperty refCatProp = cm.getReferencedCategoryProperty();
		if (refCatProp == null) { //happens in ICTM
			return;
		}
		RDFSNamedClass refCls = (RDFSNamedClass) term.getPropertyValue(cm.getReferencedCategoryProperty());
		if (refCls == null) {
			return;
		}
		
		IRI refIri = IRI.create(PublicIdCache.getPublicId(cm, refCls));
		
		OWLAnnotation ann = df.getOWLAnnotation(targetProp, refIri);
		OWLAnnotationAssertionAxiom annotationAssertionAxiom = df.getOWLAnnotationAssertionAxiom(cls.getIRI(), ann);

		manager.addAxiom(targetOnt, annotationAssertionAxiom);
	}
	
	private OWLAnnotationAssertionAxiom addBooleanAnnotation(OWLClass cls, OWLAnnotationProperty targetProp, boolean value) {
		OWLAnnotation ann = df.getOWLAnnotation(targetProp, df.getOWLLiteral(value));
		OWLAnnotationAssertionAxiom annotationAssertionAxiom = df.getOWLAnnotationAssertionAxiom(cls.getIRI(), ann);
		manager.addAxiom(targetOnt, annotationAssertionAxiom);
		
		return annotationAssertionAxiom;
	}
	
	private void addLanguageTermAnnotations(OWLClass cls, OWLAnnotationProperty targetProp, RDFProperty sourceProp) {
		addLanguageTermAnnotations(cls, targetProp, sourceProp, null);
	}
	
	private void addLanguageTermAnnotations(OWLClass cls, OWLAnnotationProperty targetProp, 
			RDFProperty sourceProp, OWLNamedIndividual indexType) {
		Collection<RDFResource> terms = cm.getTerms(sourceCls, sourceProp);
		for (RDFResource term : terms) {
			addLanguageTermAnnotationFromTerm(cls, targetProp, sourceProp, term, indexType);
		}
	}
	
	private void addLanguageTermAnnotationFromTerm(OWLClass cls, OWLAnnotationProperty targetProp, 
			RDFProperty sourceProp, RDFResource termInst, OWLNamedIndividual indexType) {
		String label = (String) termInst.getPropertyValue(cm.getLabelProperty());
		if (label == null || isAppropriateTerm (termInst) != true) {
			return;
		}
		
		//TODO: some term instance names in old icat are malformed. Check for them, and fix them
		OWLNamedIndividual targetTermInst = df.getOWLNamedIndividual(termInst.getName()); //keep the same IRI of the term. Important
		manager.addAxiom(targetOnt, df.getOWLClassAssertionAxiom(icdapiModel.getLanguageTermCls(), targetTermInst));
		
		OWLAnnotation labelAnn = df.getOWLAnnotation(icdapiModel.getLabelProp(), df.getOWLLiteral(label, ICDAPIConstants.EN_LANG));
		OWLAnnotationAssertionAxiom labelAnnAssertAx = df.getOWLAnnotationAssertionAxiom(targetTermInst.getIRI(), labelAnn);
		manager.addAxiom(targetOnt, labelAnnAssertAx);
		
		OWLAnnotation clsAnn = df.getOWLAnnotation(targetProp, targetTermInst.getIRI());
		OWLAnnotationAssertionAxiom clsAnnAssertAx = df.getOWLAnnotationAssertionAxiom(cls.getIRI(), clsAnn);
		manager.addAxiom(targetOnt, clsAnnAssertAx);
		
		// this is a base index, i.e., narrower or syn
		if (indexType != null) { 
			OWLAnnotation indexTypeAnn = df.getOWLAnnotation(icdapiModel.getIndexTypeProp(), indexType.getIRI());
			OWLAnnotationAssertionAxiom indexTypeAnnAssertAx = df.getOWLAnnotationAssertionAxiom(targetTermInst.getIRI(), indexTypeAnn);
			manager.addAxiom(targetOnt, indexTypeAnnAssertAx);
		}
		
		//check if it is also a base inclusion
		if (isBaseInclusion(termInst) == true) {
				OWLAnnotation ann = df.getOWLAnnotation(icdapiModel.isInclusionProp(), df.getOWLLiteral(true));
				OWLAnnotationAssertionAxiom annAssertAx = df.getOWLAnnotationAssertionAxiom(targetTermInst.getIRI(), ann);
				manager.addAxiom(targetOnt, annAssertAx);
		}
		
	}
		
	
	@SuppressWarnings("deprecation")
	private boolean isBaseInclusion(RDFResource termInst) {
		return termInst.hasDirectType(cm.getTermBaseInclusionClass());
	}

	
	private void deprecateCls(OWLClass cls) {
		addBooleanAnnotation(cls, df.getOWLAnnotationProperty(OWLRDFVocabulary.OWL_DEPRECATED.getIRI()), true);
	}
	
	/**
	 * Only terms with language "en" should be exported.
	 * The rest of the translations will come from the translation tool.
	 * 
	 * @param termInst
	 * @return
	 */
	private boolean isAppropriateTerm(RDFResource termInst) {
		RDFProperty langProp = cm.getLangProperty();
		
		String lang = (String) termInst.getPropertyValue(langProp);
		
		return lang == null || "en".equals(lang);
	}
	
}
