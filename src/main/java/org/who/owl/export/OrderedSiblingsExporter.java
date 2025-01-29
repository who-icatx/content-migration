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

public class OrderedSiblingsExporter {

	private final static Logger log = Logger.getLogger(OrderedSiblingsExporter.class);

	public static final String ORDERED_CHILDREN_JSON_KEY = "orderedChildren";

	private ICDContentModel cm;
	private RDFSNamedClass sourceCls;
	private JSONObject orderedChildrenJsonObject;

	public OrderedSiblingsExporter(RDFSNamedClass cls, ICDContentModel cm, JSONObject jsonObject) {
		this.cm = cm;
		this.sourceCls = cls;
		this.orderedChildrenJsonObject = jsonObject;
	}

	public void export(OWLClass targetCls) {
		Map<String, Object> orderedSiblingsMap = new HashMap<String, Object>();

		List<Map<String, String>> orderedSiblingsList = exportSiblingOrdering(targetCls);

		orderedSiblingsMap.put("entityURI", targetCls.getIRI().toString());
		orderedSiblingsMap.put("orderedChildren", orderedSiblingsList);
		
		List<Map<String, Object>> orderedSiblingsCurrentList = (List<Map<String, Object>>) orderedChildrenJsonObject.get(ORDERED_CHILDREN_JSON_KEY);
		orderedSiblingsCurrentList.add(orderedSiblingsMap);

		orderedChildrenJsonObject.put(ORDERED_CHILDREN_JSON_KEY, orderedSiblingsCurrentList);
	}

	private List<Map<String, String>> exportSiblingOrdering(OWLClass targetCls) {
		Collection<RDFResource> currentOrderedChildrenList = sourceCls.getPropertyValues(cm.getChildrenOrderProperty());
		
		List<Map<String, String>> orderedChildrenList = new ArrayList<Map<String, String>>();

		for (RDFResource orderedChildSpec : currentOrderedChildrenList) {
			Map<String, String> orderedChildMap = exportOrderedChild(orderedChildSpec);
			if (orderedChildMap != null) {
				orderedChildrenList.add(orderedChildMap);
			}
		}

		return orderedChildrenList;
	}


	private Map<String, String> exportOrderedChild(RDFResource orderedChildSpec) {

		RDFResource orderedChild = (RDFResource) orderedChildSpec.getPropertyValue(cm.getOrderedChildProperty());

		if (orderedChild == null) {
			log.warn("Invalid ordered child specification. Ordered child is null. Resource: " + orderedChildSpec +
					". Source cls: " + sourceCls.getName() + ".  Will not export.");
			return null;
		}
		
		Map<String, String> orderedChildMap = new HashMap<String, String>();

		String orderedChildPublicId = PublicIdCache.getPublicId(cm,
				cm.getOwlModel().getRDFSNamedClass(orderedChild.getName()));
		if (orderedChildPublicId != null) {
			orderedChildMap.put("orderedChild", orderedChildPublicId);
		} else {
			log.warn("Could not find public id for: " + orderedChild.getName()
					+ " used as a child ordering spec for class: " + sourceCls.getName());
		}

		Object orderedChildIndex = orderedChildSpec.getPropertyValue(cm.getOrderedChildIndexProperty());
		
		if (orderedChildIndex == null) {
			log.warn("Invalid ordered child specification. Index property value is null. Resource: " + orderedChildSpec +
					". Source cls: " + sourceCls.getName() + ".  Will not export.");
			return null;
		} else {
			orderedChildMap.put("orderedChildIndex", orderedChildIndex.toString());
		}

		return orderedChildMap;
	}
	

}
