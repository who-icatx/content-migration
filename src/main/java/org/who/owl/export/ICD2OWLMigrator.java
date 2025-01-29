package org.who.owl.export;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AddImport;
import org.semanticweb.owlapi.model.AddOntologyAnnotation;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLImportsDeclaration;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.util.SimpleIRIMapper;

import edu.stanford.bmir.whofic.icd.ICDContentModel;
import edu.stanford.smi.protege.exception.OntologyLoadException;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.util.SystemUtilities;
import edu.stanford.smi.protegex.owl.ProtegeOWL;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.RDFSNamedClass;

public class ICD2OWLMigrator {

	private final static Logger log = Logger.getLogger(ICD2OWLMigrator.class);
	
	private static String CM_FILE_PATH = "contentmodel/contentmodel.owl";

	private OWLModel sourceOnt;

	private OWLOntologyManager manager;
	private OWLOntology targetOnt;

	private RDFSNamedClass sourceTopClass;

	private ICDContentModel cm;
	private ExcludedClasses excludedClasses;
	private ICDAPIModel icdapiModel;

	private Set<RDFSNamedClass> traversed = new HashSet<RDFSNamedClass>();
	private Map<RDFSNamedClass, OWLClass> source2targetCls = new HashMap<RDFSNamedClass, OWLClass>();

	private int exportedClassesCount = 0;
	private boolean isICTM = false;
	
	private JSONObject linearizationJsonObject;
	private JSONObject pcSpecJsonObject;
	private JSONObject pcCustomScaleJsonObject;
	private JSONObject orderedSiblingsJsonObject;

	public ICD2OWLMigrator(OWLModel sourceOnt, OWLOntologyManager manager, ICDAPIModel icdapiModel,
			OWLOntology targetOnt, RDFSNamedClass sourceTopClass, 
			JSONObject linJsonObject, JSONObject pcSpecJsonObject, JSONObject pcCustomScaleJsonObject,
			JSONObject orderedSiblingsJsonObject) {
		this.sourceOnt = sourceOnt;
		this.manager = manager;
		this.targetOnt = targetOnt;
		this.sourceTopClass = sourceTopClass;
		this.cm = new ICDContentModel(sourceOnt);
		this.excludedClasses = new ExcludedClasses(sourceOnt, cm);
		this.icdapiModel = icdapiModel;
		this.isICTM = ICTMUtil.isICTMOntology(sourceOnt);
		this.linearizationJsonObject = linJsonObject;
		this.pcSpecJsonObject = pcSpecJsonObject;
		this.pcCustomScaleJsonObject = pcCustomScaleJsonObject;
		this.orderedSiblingsJsonObject = orderedSiblingsJsonObject;
	}

	public static void main(String[] args) {
		if (args.length < 3) {
			log.error("Requires 3 parameters: (1) ICD pprj or OWL file, " + "(2) top ICD class to export,  "
					+ "(3) output OWL file ");
			return;
		}
		
		//PropertyConfigurator.configure("log4j.properties");

		String sourceICDPrjFile = args[0];

		OWLModel sourceICDOnt = openOWLFile(sourceICDPrjFile);
		if (sourceICDOnt == null) {
			log.error("Could not open ICD project " + sourceICDPrjFile);
			System.exit(1);
		}

		RDFSNamedClass sourceICDTopClass = sourceICDOnt.getRDFSNamedClass(args[1]);
		if (sourceICDTopClass == null) {
			log.error("Could not find ICD top class " + args[1]);
			System.exit(1);
		}

		String outputOWLFile = args[2];

		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology targetOnt = initTargetOnt(manager, outputOWLFile);
		
		ICDAPIModel icdapiModel = new ICDAPIModel(manager, targetOnt);
		
		initTargetOntMetadata(manager,targetOnt, icdapiModel);
		
		SystemUtilities.logSystemInfo();
		
		JSONObject linJsonObj = new JSONObject();
		linJsonObj.put(LinearziationExporter.WHOFIC_LINEARIZATION_SPECIFICATIONS_JSON_KEY, new ArrayList<Map<String, Object>>());

		JSONObject pcSpecJsonObj = new JSONObject();
		pcSpecJsonObj.put(PostcoordinationSpecificationExporter.WHOFIC_POSTCOORDINATION_SPECIFICATIONS_JSON_KEY, new ArrayList<Map<String, Object>>());

		JSONObject pcCustomScaleJsonObj = new JSONObject();
		pcCustomScaleJsonObj.put(PostcoordinationCustomScaleExporter.WHOFIC_POSTCOORDINATION_SCALE_CUSTOMIZATION_JSON_KEY, new ArrayList<Map<String, Object>>());

		JSONObject orderedSiblingsJsonObj = new JSONObject();
		orderedSiblingsJsonObj.put(OrderedSiblingsExporter.ORDERED_CHILDREN_JSON_KEY, new ArrayList<Map<String, Object>>());
		
		exportOntology("ICD", sourceICDOnt, manager, icdapiModel, targetOnt, sourceICDTopClass, 
				outputOWLFile, 
				linJsonObj, pcSpecJsonObj, pcCustomScaleJsonObj, orderedSiblingsJsonObj);
		
		log.info("Starting post-processing ..");
		try {
			ExportPostProcessor postProcessor = new ExportPostProcessor(targetOnt, manager, icdapiModel);
			postProcessor.postprocess();
		} catch (Throwable e) {
			log.warn("Error at postprocessing", e);
		}
		log.info("Ended post-processing");

		
		log.info("Saving ontology..");
		try {
			manager.saveOntology(targetOnt, IRI.create(new File(outputOWLFile)));
		} catch (OWLOntologyStorageException e) {
			log.error("Error at saving ontology", e);
		}
		
		saveJsonFile(args[2], linJsonObj, ".lin.json");
		saveJsonFile(args[2], pcSpecJsonObj, ".pcSpec.json");
		saveJsonFile(args[2], pcCustomScaleJsonObj, ".pcCustomScale.json");
		saveJsonFile(args[2], orderedSiblingsJsonObj, ".orderedSiblings.json");
		

		log.info("\n===== End export at " + new Date());
	}



	private static void saveJsonFile(String owlFilePath, JSONObject jsonObj, String extensionReplacement) {
		String linfile = owlFilePath.replace(".owl", extensionReplacement);
		log.info("Saving JSON file to: " + linfile);
		
		try (FileWriter fileWriter = new FileWriter(linfile)) {

			jsonObj.writeJSONString(fileWriter);
			fileWriter.flush();
			fileWriter.close();

		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	

	private static void exportOntology(String ontShortName, OWLModel sourceOnt, OWLOntologyManager manager,
			ICDAPIModel icdapiModel, OWLOntology targetOnt, RDFSNamedClass sourceTopClass, 
			String outputOWLFile, 
			JSONObject jsonObject, JSONObject pcSpecJsonObj, JSONObject pcCustomScaleJsonObj,
			JSONObject orderedSiblingsJsonObj) {

		log.info("Started the " + ontShortName + " export");
		log.info("Top class: " + sourceTopClass.getBrowserText());
		log.info("Output file: " + outputOWLFile);

		ICD2OWLMigrator icdConv = new ICD2OWLMigrator(sourceOnt, manager, icdapiModel, targetOnt, 
				sourceTopClass, 
				jsonObject, pcSpecJsonObj, pcCustomScaleJsonObj, orderedSiblingsJsonObj);

		try {
			icdConv.export();
			icdConv.saveTargetOntology(outputOWLFile);
		} catch (Throwable t) {
			log.error(t.getMessage(), t);
		}

		log.info("Ended " + ontShortName + " export");
	}
	

	public void export() {
		export(sourceTopClass, sourceOnt.getOWLThingClass());
	}

	private void export(RDFSNamedClass sourceCls, RDFSNamedClass sourceParent) {
		/*if (excludedClasses.isExcludedTopClass(sourceCls) == true) {
			return;
		}
		*/

		if (traversed.contains(sourceCls) == true) {
			addSuperCls(sourceCls, sourceParent);
			return;
		}

		traversed.add(sourceCls);

		try {
			ClassExporter clsExporter = new ClassExporter(sourceCls, manager, targetOnt, cm, icdapiModel,
					linearizationJsonObject, pcSpecJsonObject, pcCustomScaleJsonObject, 
					orderedSiblingsJsonObject,
					isICTM);
			
			OWLClass targetCls = clsExporter.export();

			source2targetCls.put(sourceCls, targetCls);
			PublicIdCache.getPublicId(cm, sourceCls); // just fill the cache

			addChildren(sourceCls);
		} catch (Throwable t) {
			log.error("Error at adding class: " + sourceCls, t);
		}

		exportedClassesCount++;

		if (exportedClassesCount % 1000 == 0) {
			log.info("Exported " + exportedClassesCount + " classes.");
		}
	}

	private void addSuperCls(RDFSNamedClass sourceCls, RDFSNamedClass sourceParent) {
		OWLClass targetCls = getTargetCls(sourceCls);
		OWLClass targetParent = getTargetCls(sourceParent);
		OWLSubClassOfAxiom subclsAxiom = manager.getOWLDataFactory().getOWLSubClassOfAxiom(targetCls, targetParent);
		manager.addAxiom(targetOnt, subclsAxiom);
	}


	private void addChildren(RDFSNamedClass sourceCls) {
		Set<RDFSNamedClass> subclses = getNamedSubclasses(sourceCls);
		boolean isRetiredTopParent = excludedClasses.hasRetiredTitle(sourceCls);
		for (RDFSNamedClass subcls : subclses) {
			if (excludedClasses.hasRetiredTitle(subcls) == true) { //export the retired top parents
				addSuperCls(subcls, sourceCls);
				export(subcls, sourceCls);
			}

			//do not export truly retired classes. Do this check only in retired branches
			if (isRetiredTopParent == true && excludedClasses.isRetired(subcls) == true) {
				continue;
			}
			
			addSuperCls(subcls, sourceCls);
			export(subcls, sourceCls);
		}
	}

	// *************** Generic methods *************/

	private Set<RDFSNamedClass> getNamedSubclasses(RDFSNamedClass sourceCls) {
		Set<RDFSNamedClass> namedSubclses = new HashSet<RDFSNamedClass>();
		for (Object subcls : sourceCls.getSubclasses(false)) {
			if (subcls instanceof RDFSNamedClass) {
				namedSubclses.add((RDFSNamedClass) subcls);
			}
		}
		return namedSubclses;
	}

	private void saveTargetOntology(String targetPath) throws OWLOntologyStorageException {
		manager.saveOntology(targetOnt, IRI.create(new File(targetPath)));
	}

	private OWLClass getTargetCls(RDFSNamedClass cls) {
		OWLClass targetCls = source2targetCls.get(cls);

		if (targetCls == null) {
			targetCls = manager.getOWLDataFactory().getOWLClass(IRI.create(PublicIdCache.getPublicId(cm, cls)));
			source2targetCls.put(cls, targetCls);
		}

		return targetCls;
	}

	private static OWLModel openOWLFile(String fileName) {
		OWLModel owlModel = null;

		if (fileName.endsWith(".pprj")) { // pprj file
			@SuppressWarnings("rawtypes")
			List errors = new ArrayList();
			Project prj = Project.loadProjectFromFile(fileName, errors);
			if (errors.size() > 0) {
				log.error("There were errors at loading project: " + fileName);
				return null;
			}
			owlModel = (OWLModel) prj.getKnowledgeBase();
		} else { // Assume OWL file
			try {
				owlModel = ProtegeOWL.createJenaOWLModelFromURI(fileName);
			} catch (OntologyLoadException e) {
				log.error(e.getMessage(), e);
			}
		}
		return owlModel;
	}

	private static OWLOntology initTargetOnt(OWLOntologyManager manager, String outputOntFilePath) {
		File outputOntFile = new File(outputOntFilePath);
		OWLOntology targetOnt = null;

		if (outputOntFile.exists() == true) { // target ont file already exists
			log.info("Loading existing target ontology from " + outputOntFile.getAbsolutePath() +
					". The migrated content will be addded to this file.");
			
			try {
				targetOnt = manager.loadOntologyFromOntologyDocument(outputOntFile);
			} catch (OWLOntologyCreationException e) {
				log.error("Could not load target ontology from: " + outputOntFile.getAbsolutePath(), e);
			}
		} else { // target ont file does not exist, import the content model file
			File cmOntFile = new File(CM_FILE_PATH);
			
			if (cmOntFile.exists() == true) { // start with template ont file
				log.info("Creating new ontology at: " + outputOntFile.getAbsolutePath());
				
				targetOnt = loadFromExistingFile(manager, outputOntFile);
			} else { // content model file does not exist, initialize empty target ont
				try {
					log.info("Creating new target ontology (no content model ontology was found)");
					targetOnt = manager.createOntology(IRI.create(ICDAPIConstants.TARGET_ONT_NAME));
				} catch (OWLOntologyCreationException e) {
					log.error("Could not create target OWL ontology", e);
					System.exit(1);
				}
			}
		}

		return targetOnt;
	}
	
	
	private static OWLOntology loadFromExistingFile(OWLOntologyManager manager, File outputOntFile) {
		OWLOntology targetOnt = null;
		
		try {
		     targetOnt = manager.createOntology(IRI.create(ICDAPIConstants.TARGET_ONT_NAME));
		     
		     IRI localCMIRI = IRI.create(CM_FILE_PATH);
		     IRI remoteCMIRI = IRI.create(ICDAPIConstants.CM_ONT_NAME);
		     
		     log.info("Importing content model from: " + localCMIRI);
		     
		     manager.getIRIMappers().add(new SimpleIRIMapper(remoteCMIRI, localCMIRI));
			
		     OWLImportsDeclaration importDeclaration=manager.getOWLDataFactory().getOWLImportsDeclaration(remoteCMIRI);
		     manager.applyChange(new AddImport(targetOnt, importDeclaration));
		     
		     log.info("Saving target ontology to: " + outputOntFile.getAbsolutePath());
		     IRI outputOntIRI = IRI.create(outputOntFile);
		    
		     manager.saveOntology(targetOnt, outputOntIRI);
		//     targetOnt = manager.loadOntology(outputOntIRI);
		     
		} catch (OWLOntologyCreationException e) {
			log.error("Could not load target OWL ontology from: " + outputOntFile.getAbsolutePath(), e);
		} catch (OWLOntologyStorageException e) {
			log.error("Could not save target OWL ontology to: " + outputOntFile.getAbsolutePath(), e);
		}
		
		return targetOnt;
	}

	private static void initTargetOntMetadata(OWLOntologyManager manager, OWLOntology targetOnt, ICDAPIModel icdapiModel) {
		OWLDataFactory df = manager.getOWLDataFactory();
		
		//adding the release date
		String dateStr = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
		
		OWLAnnotation releaseDateAnn = df.getOWLAnnotation(icdapiModel.getReleaseDate(), df.getOWLLiteral(dateStr));
		manager.applyChange(new AddOntologyAnnotation(targetOnt, releaseDateAnn));
		
		OWLAnnotation owlVersionAnn = df.getOWLAnnotation(df.getOWLVersionInfo(), df.getOWLLiteral(dateStr));
		manager.applyChange(new AddOntologyAnnotation(targetOnt, owlVersionAnn));
	}
}
