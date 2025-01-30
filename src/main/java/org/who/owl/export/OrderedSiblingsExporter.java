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


	/**
	 * This method will create the JSON structure for one ordered child {@code [orderedChild, orderedChildIndex]},
	 * which is part of the {@code orderedChildren} list.
	 * <pre>{@code
	 * 
	 * "orderedChildren": [
    {
        "entityURI": "http://id.who.int/icd/entity/257068234", //Bacterial intestinal infections - parent 1 of Cholera
        
        "orderedChildren": [
            {
                "orderedChild": "http://id.who.int/icd/entity/257068234", //Cholera
                "orderedChildIndex": "2000000",
            },
            
            {
                "orderedChild": "http://id.who.int/icd/entity/1561949126", //Intestinal infection due to other Vibrio
                "orderedChildIndex": "2500000",
            },
            ...
         ]
	 * }
	 * </pre>
	 * 
	 * If the orderedChild or the orderedChildIndex are missing, then the entire orderedChildren will not be added in JSON.
	 * Alternatively, you can call:
	 * 
	 * <pre>
	 * 		SiblingReordering so = new SiblingReordering(cm);
	 * 		so.checkIndexAndRecreate(sourceCls, true);
	 * </pre>
	 * 
	 * which will recreate the index for sourceCls, but it will also be a write operation in the KB.
	 * 
	 * @param orderedChildSpec
	 * @return
	 */
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
