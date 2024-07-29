package org.who.owl.export;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
				linSpecList.add(linMap);
			}
		}

		return linSpecList;
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

		linMap.put("linearizationView", newLinViewName);

		RDFResource linParent = (RDFResource) linSpec.getPropertyValue(cm.getLinearizationParentProperty());

		if (linParent != null) {
			String parentPublicId = PublicIdCache.getPublicId(cm,
					cm.getOwlModel().getRDFSNamedClass(linParent.getName()));
			if (parentPublicId != null) {
				linMap.put("linearizationParent", parentPublicId);
			} else {
				log.warn("Could not find public id for JSON export for: " + linParent.getName()
						+ " used as a linearization parent for class: " + sourceCls.getName());
			}
		}

		RDFResource codingNoteTerm = (RDFResource) linSpec.getPropertyValue(cm.getCodingNoteProperty());

		if (codingNoteTerm != null) {
			String codingNote = (String) codingNoteTerm.getPropertyValue(cm.getLabelProperty());
			if (codingNote != null) {
				linMap.put("codingNote", codingNote);
			}
		}

		exportBooleanLinProp(linSpec, cm.getIsIncludedInLinearizationProperty(), "isIncludedInLinearization", linMap);
		exportBooleanLinProp(linSpec, cm.getIsGroupingProperty(), "isGrouping", linMap);
		exportBooleanLinProp(linSpec, cm.getIsAuxiliaryAxisChildProperty(), "isAuxiliaryAxisChild", linMap);

		return linMap;
	}
	

	private void exportBooleanLinProp(RDFResource linSpec, RDFProperty prop, String jsonKey, Map<String, String> map) {
		Boolean bool = (Boolean) linSpec.getPropertyValue(prop);

		String value = bool == null ? "unknown" : bool.toString();
		
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
