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
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protegex.owl.model.RDFProperty;
import edu.stanford.smi.protegex.owl.model.RDFResource;
import edu.stanford.smi.protegex.owl.model.RDFSNamedClass;

public class PostcoordinationCustomScaleExporter {
	
	private final static Logger log = Logger.getLogger(PostcoordinationCustomScaleExporter.class);

	public static final String WHOFIC_POSTCOORDINATION_SCALE_CUSTOMIZATION_JSON_KEY = "postcoordinationScaleCustomization";
	public static final String DESCRIPTION_PROP = "http://who.int/icd#description";
	
	private ICDContentModel cm;
	private RDFSNamedClass sourceCls;
	private JSONObject pcCustomScaleJsonObject;
	
	private RDFProperty postCoordinationAxis;
	private RDFProperty descriptionProp;

	public PostcoordinationCustomScaleExporter(RDFSNamedClass cls, ICDContentModel cm, JSONObject jsonObject) {
		this.cm = cm;
		this.sourceCls = cls;
		this.pcCustomScaleJsonObject = jsonObject;
		this.postCoordinationAxis = cm.getOwlModel().getRDFProperty(ICDPostCoordinationMaps.POSTCOORDINATION_AXIS_PROP);
		this.descriptionProp = cm.getOwlModel().getRDFProperty(DESCRIPTION_PROP);
	}
	
	public void export(OWLClass targetCls) {
		
		Map<String, Object> pcSpecMap = new HashMap<String, Object>();

		List<Map<String, Object>> scaleCustomizationList = exportScaleCustomizations(targetCls);

		if (scaleCustomizationList == null || scaleCustomizationList.isEmpty() == true) {
			return;
		}
		
		pcSpecMap.put("whoficEntityIri", targetCls.getIRI().toString());
		pcSpecMap.put("scaleCustomizations", scaleCustomizationList);
		
		List<Map<String, Object>> pcSpecsList = (List<Map<String, Object>>) pcCustomScaleJsonObject.get(WHOFIC_POSTCOORDINATION_SCALE_CUSTOMIZATION_JSON_KEY);
		pcSpecsList.add(pcSpecMap);

		pcCustomScaleJsonObject.put(WHOFIC_POSTCOORDINATION_SCALE_CUSTOMIZATION_JSON_KEY, pcSpecsList);
		
	}
	
	
	private List<Map<String, Object>> exportScaleCustomizations(OWLClass targetCls) {
		List<Map<String, Object>> scaleCustomizationList = new ArrayList<Map<String, Object>>();
		
		//not efficient, but who cares
		Collection<Slot> ownSlots = sourceCls.getOwnSlots();
		
		for (Slot ownSlot : ownSlots) {
			if (ownSlot instanceof RDFProperty && isPostCoordinationProperty((RDFProperty)ownSlot) == true) {
				Map<String, Object> customScaleMap = exportCustomScale((RDFProperty)ownSlot);
				if (customScaleMap != null && customScaleMap.isEmpty() == false) {
					scaleCustomizationList.add(customScaleMap);
				}
			}
		}
		
		return scaleCustomizationList;
	}

	private Map<String, Object> exportCustomScale(RDFProperty pcProp) {
		Collection<RDFResource> refTerms = sourceCls.getDirectOwnSlotValues(pcProp);
		if (refTerms == null) {
			return null;
		}
		
		Map<String, Object> customScaleMap = new HashMap<String, Object>();
		customScaleMap.put("postcoordinationAxis", getPostcoordinationAxisName(pcProp.getName()));
		
		List<String> customScaleValueList = new ArrayList<String>();
		
		for (RDFResource refTerm : refTerms) {
			RDFSNamedClass refValue = (RDFSNamedClass) refTerm.getPropertyValue(cm.getReferencedValueProperty());
			if (refValue != null) {
				String publicId = PublicIdCache.getPublicId(cm, refValue);
				if (publicId == null) {
					log.warn("Could not find public id for: " + refValue + ", " + refValue.getBrowserText() + ", while exporting PC custom scale values for pc axis: " + pcProp);
					continue;
				} else {
					customScaleValueList.add(publicId);
				}
			}
		}
		
		if (customScaleValueList.isEmpty() == true) {
			return null;
		}
		
		customScaleMap.put("postcoordinationScaleValues", customScaleValueList);
		
		return customScaleMap;
	}

	
	private boolean isPostCoordinationProperty(RDFProperty prop) {
		return prop.hasSuperslot(postCoordinationAxis) == true;
	}
	
	private String getPostcoordinationAxisName(String propName) {
		return propName.replace(ICDAPIConstants.SOURCE_ONT_NS, ICDAPIConstants.TARGET_POSTCOORDINATION_NS);
	}

	
}
