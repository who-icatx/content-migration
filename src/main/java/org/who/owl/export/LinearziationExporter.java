package org.who.owl.export;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.semanticweb.owlapi.model.OWLClass;

import edu.stanford.bmir.whofic.icd.ICDContentModel;
import edu.stanford.smi.protegex.owl.model.RDFProperty;
import edu.stanford.smi.protegex.owl.model.RDFResource;
import edu.stanford.smi.protegex.owl.model.RDFSNamedClass;

public class LinearziationExporter {

	private final static Logger log = Logger.getLogger(LinearziationExporter.class);

	public static final String WHOFIC_LINEARIZATION_SPECIFICATIONS_JSON_KEY = "whoficEntityLinearizationSpecification";

	private static final String LINEARIZATION_VIEW_JSON_KEY = "linearizationView";
	private static final String IS_INCLUDED_IN_LINEARIZATION_JSON_KEY = "isIncludedInLinearization";
	private static final String IS_GROUPING_JSON_KEY = "isGrouping";
	private static final String IS_AUXILIARY_AXIS_CHILD_JSON_KEY = "isAuxiliaryAxisChild";
	private static final String LINEARIZATION_PARENT_JSON_KEY = "linearizationParent";
	private static final String CODING_NOTE_JSON_KEY = "codingNote";

	private static final String UNKNOWN = "unknown";

	private ICDContentModel cm;
	private RDFSNamedClass sourceCls;
	private JSONObject linJsonObject;

	public LinearziationExporter(RDFSNamedClass cls, ICDContentModel cm, JSONObject jsonObject) {
		this.cm = cm;
		this.sourceCls = cls;
		this.linJsonObject = jsonObject;
	}

	public void export(OWLClass targetCls) {
		Map<String, Object> linSpecMap = new HashMap<String, Object>();

		Map<String, String> residualsMap = exportResiduals(targetCls);
		List<Map<String, String>> linSpecList = exportLinSpecs(targetCls);

		linSpecMap.put("whoficEntityIri", targetCls.getIRI().toString());
		linSpecMap.put("linearizationSpecifications", linSpecList);
		
		if (residualsMap.isEmpty() == false) {
			linSpecMap.put("linearizationResiduals", residualsMap);
		}

		List<Map<String, Object>> linList = (List<Map<String, Object>>) linJsonObject.get(WHOFIC_LINEARIZATION_SPECIFICATIONS_JSON_KEY);
		linList.add(linSpecMap);

		linJsonObject.put(WHOFIC_LINEARIZATION_SPECIFICATIONS_JSON_KEY, linList);
	}

	private List<Map<String, String>> exportLinSpecs(OWLClass targetCls) {
		Collection<RDFResource> linSpecs = cm.getLinearizationSpecifications(sourceCls);
		List<Map<String, String>> linSpecList = new ArrayList<Map<String, String>>();

		for (RDFResource linSpec : linSpecs) {
			Map<String, String> linMap = exportLinearization(linSpec);
			if (linMap != null) {
				Optional<Map<String, String>> altLinMap = getDuplicateLinMap(linSpecList, linMap);
				if (altLinMap.isPresent()) {
					updateExistingLinSpec(targetCls, altLinMap.get(), linMap);
				}
				else {
					linSpecList.add(linMap);
				}
			}
		}

		return linSpecList;
	}

	private Optional<Map<String, String>> getDuplicateLinMap(List<Map<String, String>> linSpecList, Map<String, String> linMap) {
		String linViewName = linMap.get(LINEARIZATION_VIEW_JSON_KEY);
		
        Optional<Map<String, String>> result = linSpecList.stream()
                .filter(m -> linViewName.equals(m.get(LINEARIZATION_VIEW_JSON_KEY)))
                .findFirst();
        
		return result;
	}

	private void updateExistingLinSpec(OWLClass targetCls, Map<String, String> firstLinMap, Map<String, String> secondLinMap) {
		boolean isTelescopic = LinearizationViewMap.isTelescopicLinearization(firstLinMap.get(LINEARIZATION_VIEW_JSON_KEY));
		
		String isPartOf1 = firstLinMap.get(IS_INCLUDED_IN_LINEARIZATION_JSON_KEY);
		String isPartOf2 = secondLinMap.get(IS_INCLUDED_IN_LINEARIZATION_JSON_KEY);
//		if (! isTelescopic) {
			if (UNKNOWN.equals(isPartOf1)) {
				firstLinMap.put(IS_INCLUDED_IN_LINEARIZATION_JSON_KEY, isPartOf2);
			}
			else if (UNKNOWN.equals(isPartOf2)) {
				//do nothing (i.e. keep isPartOf1)
			}
			else if (! isPartOf1.equals(isPartOf2)) {
				log.warn("Conflicting values (" + isPartOf1 + "/" + isPartOf2 + ") for " + IS_INCLUDED_IN_LINEARIZATION_JSON_KEY + " for linearization view " +
						firstLinMap.get(LINEARIZATION_VIEW_JSON_KEY) + " on entity " + targetCls);
			}
//		}
//		else {
//			
//		}
			
		String isGrouping1 = firstLinMap.get(IS_GROUPING_JSON_KEY);
		String isGrouping2 = secondLinMap.get(IS_GROUPING_JSON_KEY);
//		if (! isTelescopic) {
			if (UNKNOWN.equals(isGrouping1)) {
				firstLinMap.put(IS_GROUPING_JSON_KEY, isGrouping2);
			}
			else if (UNKNOWN.equals(isGrouping2)) {
				//do nothing (i.e. keep isGrouping1)
			}
			else if (! isGrouping1.equals(isGrouping2)) {
				log.warn("Conflicting values (" + isGrouping1 + "/" + isGrouping2 + ") for " + IS_GROUPING_JSON_KEY + " for linearization view " +
						firstLinMap.get(LINEARIZATION_VIEW_JSON_KEY) + " on entity " + targetCls);
			}
//		}
//		else {
//			
//		}
			
		String isAuxCh1 = firstLinMap.get(IS_AUXILIARY_AXIS_CHILD_JSON_KEY);
		String isAuxCh2 = secondLinMap.get(IS_AUXILIARY_AXIS_CHILD_JSON_KEY);
//		if (! isTelescopic) {
			if (UNKNOWN.equals(isAuxCh1)) {
				firstLinMap.put(IS_AUXILIARY_AXIS_CHILD_JSON_KEY, isAuxCh2);
			}
			else if (UNKNOWN.equals(isAuxCh2)) {
				//do nothing (i.e. keep isAuxCh1)
			}
			else if (! isAuxCh1.equals(isAuxCh2)) {
				log.warn("Conflicting values (" + isAuxCh1 + "/" + isAuxCh2 + ") for " + IS_AUXILIARY_AXIS_CHILD_JSON_KEY + " for linearization view " +
						firstLinMap.get(LINEARIZATION_VIEW_JSON_KEY) + " on entity " + targetCls);
			}
//		}
//		else {
//			
//		}
		
			
		String linParent1 = firstLinMap.get(LINEARIZATION_PARENT_JSON_KEY);
		String linParent2 = secondLinMap.get(LINEARIZATION_PARENT_JSON_KEY);
		if (! isTelescopic) {
			if (linParent1 == null && linParent2 != null) {
				firstLinMap.put(LINEARIZATION_PARENT_JSON_KEY, linParent2);
			}
			else if (linParent2 == null) {
				//do nothing (i.e. keep linParent1)
			}
			else if (! linParent1.equals(linParent2)) {
				log.warn("Conflicting values (" + linParent1 + "/" + linParent2 + ") for " + LINEARIZATION_PARENT_JSON_KEY + " for linearization view " +
						firstLinMap.get(LINEARIZATION_VIEW_JSON_KEY) + " on entity " + targetCls);
			}
		}
		else {
			firstLinMap.remove(LINEARIZATION_PARENT_JSON_KEY);
			if (linParent1 != null ||linParent2 != null) {
				log.warn("Ignoring linerization parents (" + linParent1 + "/" + linParent2 + ") for TELESCOPIC linearization view " +
						firstLinMap.get(LINEARIZATION_VIEW_JSON_KEY) + " on entity " + targetCls);
			}
		}

		String notes1 = firstLinMap.get(CODING_NOTE_JSON_KEY);
		String notes2 = secondLinMap.get(CODING_NOTE_JSON_KEY);
//		if (! isTelescopic) {
			if (notes1 == null && notes2 != null) {
				firstLinMap.put(CODING_NOTE_JSON_KEY, notes2);
			}
			else if (notes2 == null) {
				//do nothing (i.e. keep notes1)
			}
			else if (! notes1.equals(notes2)) {
				log.warn("Conflicting values (" + notes1 + "/" + notes2 + ") for " + CODING_NOTE_JSON_KEY + " for linearization view " +
						firstLinMap.get(CODING_NOTE_JSON_KEY) + " on entity " + targetCls);
			}
//		}
//		else {
//		
//		}

	}

	private Map<String, String> exportResiduals(OWLClass targetCls) {
		Map<String, String> residualsMap = new HashMap<String, String>();
		
		exportStringResidualsTerm(cm.getOtherSpecifiedResidualTitleProperty(), "otherSpecifiedResidualTitle", residualsMap);
		exportStringResidualsTerm(cm.getUnspecifiedResidualTitleProperty(), "unspecifiedResidualTitle", residualsMap);

		exportBooleanResiduals(cm.getSuppressOtherSpecifiedResidualsProperty(), "suppressOtherSpecifiedResiduals", residualsMap);
		exportBooleanResiduals(cm.getSuppressUnspecifiedResidualsProperty(), "suppressUnspecifiedResiduals", residualsMap);

		return residualsMap;
	}

	private Map<String, String> exportLinearization(RDFResource linSpec) {

		RDFResource linView = (RDFResource) linSpec.getPropertyValue(cm.getLinearizationViewProperty());

		if (linView == null) {
			return null;
		}
		
		String newLinViewName = LinearizationViewMap.getNewLinearizationViewName(linView.getName());
		
		if (newLinViewName == null) {
			return null;
		}

		Map<String, String> linMap = new HashMap<String, String>();

		linMap.put(LINEARIZATION_VIEW_JSON_KEY, newLinViewName);

		RDFResource linParent = (RDFResource) linSpec.getPropertyValue(cm.getLinearizationParentProperty());

		if (linParent != null) {
			String parentPublicId = PublicIdCache.getPublicId(cm,
					cm.getOwlModel().getRDFSNamedClass(linParent.getName()));
			if (parentPublicId != null) {
				linMap.put(LINEARIZATION_PARENT_JSON_KEY, parentPublicId);
			} else {
				log.warn("Could not find public id for JSON export for: " + linParent.getName()
						+ " used as a linearization parent for class: " + sourceCls.getName());
			}
		}

		RDFResource codingNoteTerm = (RDFResource) linSpec.getPropertyValue(cm.getCodingNoteProperty());

		if (codingNoteTerm != null) {
			String codingNote = (String) codingNoteTerm.getPropertyValue(cm.getLabelProperty());
			if (codingNote != null) {
				linMap.put(CODING_NOTE_JSON_KEY, codingNote);
			}
		}

		exportBooleanLinProp(linSpec, cm.getIsIncludedInLinearizationProperty(), IS_INCLUDED_IN_LINEARIZATION_JSON_KEY, false, linMap);
		exportBooleanLinProp(linSpec, cm.getIsGroupingProperty(), IS_GROUPING_JSON_KEY, false, linMap);
		exportBooleanLinProp(linSpec, cm.getIsAuxiliaryAxisChildProperty(), IS_AUXILIARY_AXIS_CHILD_JSON_KEY, false, linMap);

		return linMap;
	}
	

	private void exportBooleanLinProp(RDFResource linSpec, RDFProperty prop, String jsonKey, 
			boolean replaceUnkownWithFalse, Map<String, String> map) {
		Boolean bool = (Boolean) linSpec.getPropertyValue(prop);

		if (bool == null && replaceUnkownWithFalse == true) {
			bool = Boolean.FALSE;
		}
		
		String value = bool == null ? UNKNOWN : bool.toString();
		
		map.put(jsonKey, value);
	}
	
	
	private void exportStringResidualsTerm(RDFProperty prop, String jsonKey, Map<String, String> map) {
		RDFResource term = (RDFResource) sourceCls.getPropertyValue(prop); 
		
		if (term == null) {
			return;
		}

		String label = (String) term.getPropertyValue(cm.getLabelProperty());
		
		if (label != null) {
			map.put(jsonKey, label);
		}
	}
	
	private void exportBooleanResiduals(RDFProperty prop, String jsonKey, Map<String, String> map) {
		Boolean bool = (Boolean) sourceCls.getPropertyValue(prop);

		if (bool == null) {
			return;
		}

		map.put(jsonKey, bool.toString());
	}

}
