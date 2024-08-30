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
import edu.stanford.smi.protegex.owl.model.RDFResource;
import edu.stanford.smi.protegex.owl.model.RDFSNamedClass;

public class PostcoordinationSpecificationExporter {
	
	private final static Logger log = Logger.getLogger(PostcoordinationSpecificationExporter.class);

	public static final String WHOFIC_POSTCOORDINATION_SPECIFICATIONS_JSON_KEY = "whoficEntityPostcoordinationSpecification";

	private ICDContentModel cm;
	private RDFSNamedClass sourceCls;
	private JSONObject pcSpecJsonObject;

	public PostcoordinationSpecificationExporter(RDFSNamedClass cls, ICDContentModel cm, JSONObject jsonObject) {
		this.cm = cm;
		this.sourceCls = cls;
		this.pcSpecJsonObject = jsonObject;
	}
	
	public void export(OWLClass targetCls) {
		
		Map<String, Object> pcSpecMap = new HashMap<String, Object>();

		List<Map<String, Object>> pcSpecList = exportPcSpecs(targetCls);

		pcSpecMap.put("whoficEntityIri", targetCls.getIRI().toString());
		pcSpecMap.put("postcoordinationSpecifications", pcSpecList);
		
		List<Map<String, Object>> pcSpecsList = (List<Map<String, Object>>) pcSpecJsonObject.get(WHOFIC_POSTCOORDINATION_SPECIFICATIONS_JSON_KEY);
		pcSpecsList.add(pcSpecMap);

		pcSpecJsonObject.put(WHOFIC_POSTCOORDINATION_SPECIFICATIONS_JSON_KEY, pcSpecsList);

	}

	private List<Map<String, Object>> exportPcSpecs(OWLClass targetCls) {
		List<Map<String, Object>> pcSpecList = new ArrayList<Map<String, Object>>();
		
		Collection<RDFResource>  allowedPCSpecs = cm.getAllowedPostcoordinationSpecifications(sourceCls);
		
		for (RDFResource allowedPCSpec : allowedPCSpecs) {
			Map<String, Object> pcSpecMap = exportPCSpec(allowedPCSpec);
			if (pcSpecMap != null) {
				pcSpecList.add(pcSpecMap);
			}
		}
		
		return pcSpecList;
	}

	private Map<String, Object> exportPCSpec(RDFResource pcSpec) {
		Map<String, Object> pcSpecMap = new HashMap<String, Object>();
		
		RDFResource linView = cm.getLinearizationViewForSpec(pcSpec);
		
		if (linView == null) {
			return null;
		}
		
		String newLinViewName = LinearizationViewMap.getNewLinearizationViewName(linView.getName());
		
		if (newLinViewName == null) {
			return null;
		}
		
		pcSpecMap.put("linearizationView", newLinViewName);
		
		@SuppressWarnings("deprecation") //using this method because, otherwise it would return also the required axes (which is a subprop of the allowed axes)
		Collection<RDFResource> allowedAxes = (Collection<RDFResource>) pcSpec.getDirectOwnSlotValues(cm.getAllowedPostcoordinationAxisPropertyProperty());
		
		if (allowedAxes != null && allowedAxes.isEmpty() == false) {
			pcSpecMap.put("allowedAxes", getAxesNameList(allowedAxes));
		}
		
		Collection<RDFResource> requiredAxes = (Collection<RDFResource>) pcSpec.getPropertyValues(cm.getRequiredPostcoordinationAxisPropertyProperty());
		
		if (requiredAxes != null && requiredAxes.isEmpty() == false) {
			pcSpecMap.put("requiredAxes", getAxesNameList(requiredAxes));
		}
		
		return pcSpecMap;
	}
	
	private List<String> getAxesNameList(Collection<RDFResource> resList) {
		List<String> names = new ArrayList<String>();
		for (RDFResource res : resList) {
			String axisSourceName = res.getName();
			String targetName = axisSourceName.replace(ICDAPIConstants.SOURCE_ONT_NS, ICDAPIConstants.TARGET_POSTCOORDINATION_NS);
			names.add(targetName);
		}
		
		return names;
	}
	
	
}
