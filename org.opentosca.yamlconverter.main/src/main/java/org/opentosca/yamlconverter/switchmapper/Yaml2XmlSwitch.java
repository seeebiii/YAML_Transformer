package org.opentosca.yamlconverter.switchmapper;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.namespace.QName;

import org.opentosca.model.tosca.Definitions;
import org.opentosca.model.tosca.TCapabilityDefinition;
import org.opentosca.model.tosca.TCapabilityType;
import org.opentosca.model.tosca.TDocumentation;
import org.opentosca.model.tosca.TEntityTemplate;
import org.opentosca.model.tosca.TEntityType.DerivedFrom;
import org.opentosca.model.tosca.TEntityType.PropertiesDefinition;
import org.opentosca.model.tosca.TInterface;
import org.opentosca.model.tosca.TNodeTemplate;
import org.opentosca.model.tosca.TNodeType;
import org.opentosca.model.tosca.TNodeType.CapabilityDefinitions;
import org.opentosca.model.tosca.TNodeType.Interfaces;
import org.opentosca.model.tosca.TNodeType.RequirementDefinitions;
import org.opentosca.model.tosca.TRelationshipType;
import org.opentosca.model.tosca.TRequirementDefinition;
import org.opentosca.model.tosca.TServiceTemplate;
import org.opentosca.model.tosca.TTopologyTemplate;
import org.opentosca.yamlconverter.main.AnyMap;
import org.opentosca.yamlconverter.yamlmodel.yaml.element.CapabilityType;
import org.opentosca.yamlconverter.yamlmodel.yaml.element.Import;
import org.opentosca.yamlconverter.yamlmodel.yaml.element.Input;
import org.opentosca.yamlconverter.yamlmodel.yaml.element.NodeTemplate;
import org.opentosca.yamlconverter.yamlmodel.yaml.element.NodeType;
import org.opentosca.yamlconverter.yamlmodel.yaml.element.RelationshipType;
import org.opentosca.yamlconverter.yamlmodel.yaml.element.ServiceTemplate;

public class Yaml2XmlSwitch {
	private static String TYPESNS = "http://www.example.org/tosca/yamlgen/types";

	private long uniqueID = 0;

	private StringBuilder xsd;
	private Map<String, String> inputReq;

	/**
	 * Parses {@link ServiceTemplate} to {@link Definitions}.
	 *
	 * @param st the {@link ServiceTemplate} to parse
	 * @return the parsed {@link Definitions}
	 */
	public Definitions parse(ServiceTemplate st) {
		this.xsd = new StringBuilder(); // reset
		this.inputReq = new HashMap<>();
		return case_ServiceTemplate(st);
	}

	/**
	 * Returns an additional XSD to support the properties of templates.
	 *
	 * @return additional XSD.
	 */
	public String getXSD() {
		return this.xsd.toString();
	}

	public Map<String, String> getInputRequirements() {
		return this.inputReq;
	}

	/**
	 * Deprecated. Use parse(..).
	 *
	 * @param elem element to parse
	 * @return parsed object
	 * @deprecated please use parse(ServiceTemplate st)
	 */
	@Deprecated
	public Object doswitch(Object elem) {
		if (elem instanceof ServiceTemplate) {
			return case_ServiceTemplate((ServiceTemplate) elem);
		}
		if (elem instanceof NodeTemplate) {
			return case_NodeTemplate((NodeTemplate) elem);
		}
		throw new UnsupportedOperationException("Object not yet supported");
	}

	private Definitions case_ServiceTemplate(ServiceTemplate elem) {
		for (final Entry<String, Input> entry : elem.getInputs().entrySet()) {
			final String value = entry.getValue().getDescription() + " Has to be of type " +
			// TODO: YAMLmodel Input
			// entry.getValue().getType() +
					"<notdefined>" + ".";
			this.inputReq.put(entry.getKey(), value);
		}

		final Definitions result = new Definitions();
		final TServiceTemplate serviceTemplate = new TServiceTemplate();
		final TTopologyTemplate topologyTemplate = new TTopologyTemplate();
		result.setId(unique("root"));
		result.setName(unique("Root"));
		// result.setTargetNamespace();
		// result.setExtensions();
		// result.setTypes();
		result.getDocumentation().add(toDocumentation(elem.getDescription()));
		// result.getOtherAttributes().put(name, attribute);
		for (final Entry<String, CapabilityType> capType : elem.getCapability_types().entrySet()) {
			final TCapabilityType ct = case_CapabilityType(capType);
			ct.setName(capType.getKey());
			result.getServiceTemplateOrNodeTypeOrNodeTypeImplementation().add(ct);
		}
		for (final Entry<String, NodeType> nt : elem.getNode_types().entrySet()) {
			final TNodeType xnode = case_NodeType(nt.getValue(), nt.getKey());
			result.getServiceTemplateOrNodeTypeOrNodeTypeImplementation().add(xnode);
		}
		for (final Entry<String, RelationshipType> relType : elem.getRelationship_types().entrySet()) {
			final TRelationshipType rt = case_RelationshipType(relType);
			rt.setName(relType.getKey());
			result.getServiceTemplateOrNodeTypeOrNodeTypeImplementation().add(rt);
		}
		// for (final ArtifactType artType : elem.getArtifactType()) {
		// result.getServiceTemplateOrNodeTypeOrNodeTypeImplementation().add(case_ArtifactType(artType));
		// }
		for (final Entry<String, Import> importelem : elem.getImports().entrySet()) {
			// TODO: How do we handle imports?
			// result.getImport().add(case_Import(importelem));
			// TODO: add types import
		}
		// serviceTemplate.setBoundaryDefinitions(value);
		serviceTemplate.setId(unique("serviceTemplate"));
		serviceTemplate.setName("ServiceTemplate");
		// serviceTemplate.setPlans(value);
		// serviceTemplate.setSubstitutableNodeType(value);
		// serviceTemplate.setTags(value);
		// serviceTemplate.setTargetNamespace(value);
		serviceTemplate.setTopologyTemplate(topologyTemplate);
		// serviceTemplate.getDocumentation().add(docu);
		// serviceTemplate.getOtherAttributes().put(key, value);
		// topologyTemplate.getAny().add(o);
		// topologyTemplate.getDocumentation().add(docu);
		for (final Map.Entry<String, NodeTemplate> nt : elem.getNode_templates().entrySet()) {
			final TNodeTemplate xnode = case_NodeTemplate(nt.getValue());
			// override name and id of the nodetemplate
			xnode.setName(nt.getKey());
			xnode.setId(name2id(nt.getKey()));
			topologyTemplate.getNodeTemplateOrRelationshipTemplate().add(xnode);
		}
		// topologyTemplate.getOtherAttributes().put(key, value);
		return result;
	}

	private TRelationshipType case_RelationshipType(Entry<String, RelationshipType> relType) {
		final TRelationshipType result = new TRelationshipType();
		// TODO: YAMLmodel RelationshipType
		// result.setAbstract(value);
		// result.setDerivedFrom(value);
		// result.setFinal(value);
		// result.setInstanceStates(value);
		// result.setName(value);
		// result.setPropertiesDefinition(value);
		// result.setSourceInterfaces(value);
		// result.setTags(value);
		// result.setTargetInterfaces(value);
		// result.setTargetNamespace(value);
		// result.setValidSource(value);
		// result.setValidTarget(value);
		// result.getAny().add(e);
		result.getDocumentation().add(toDocumentation(relType.getValue().getDescription()));
		// result.getOtherAttributes().put(key, value);
		return result;
	}

	private TCapabilityType case_CapabilityType(Entry<String, CapabilityType> capType) {
		final TCapabilityType result = new TCapabilityType();
		// TODO: YAMLmodel CapabilityType
		// result.setAbstract(value);
		// result.setDerivedFrom(value);
		// result.setFinal(value);
		// result.setName(value);
		// result.setPropertiesDefinition(value);
		// result.setTags(value);
		// result.setTargetNamespace(value);
		// result.getAny().add();
		result.getDocumentation().add(toDocumentation(capType.getValue().getDescription()));
		// result.getOtherAttributes().put(key, value);
		return result;
	}

	private TNodeType case_NodeType(NodeType value, String name) {
		final TNodeType result = new TNodeType();
		// TODO: value.getArtifacts() ?
		// result.setAbstract(value);
		result.setCapabilityDefinitions(parseNodeTypeCapabilities(value.getCapabilities()));
		result.setDerivedFrom(parseNodeTypeDerivedFrom(value.getDerived_from()));
		// result.setFinal(value);
		// result.setInstanceStates(value);
		result.setInterfaces(parseNodeTypeInterfaces(value.getInterfaces()));
		result.setName(name);
		result.setPropertiesDefinition(parseNodeTypePropertiesDefinition(value.getProperties(), name));
		result.setRequirementDefinitions(parseNodeTypeRequirementDefinitions(value.getRequirements()));
		// result.setTags(value);
		// result.setTargetNamespace(value);
		result.getDocumentation().add(toDocumentation(value.getDescription()));
		// result.getAny().add(e);
		// result.getOtherAttributes().put(key, value);
		return result;
	}

	private RequirementDefinitions parseNodeTypeRequirementDefinitions(Map<String, String> requirements) {
		final RequirementDefinitions result = new RequirementDefinitions();
		for (final Entry<String, String> req : requirements.entrySet()) {
			final TRequirementDefinition rd = new TRequirementDefinition();
			// rd.setConstraints(value);
			// rd.setLowerBound(value);
			rd.setName(req.getKey());
			rd.setRequirementType(new QName(req.getValue()));
			// rd.setUpperBound(value);
			result.getRequirementDefinition().add(rd);
		}
		return result;
	}

	private PropertiesDefinition parseNodeTypePropertiesDefinition(Map<String, String> properties, String typename) {
		final PropertiesDefinition result = new PropertiesDefinition();
		// TODO: setElement()?!
		// result.setElement(value);
		result.setType(new QName(TYPESNS, typename + "Properties", "types"));
		generateTypeXSD(properties, typename + "Properties");
		return result;
	}

	private void generateTypeXSD(Map<String, String> properties, String name) {
		this.xsd.append("<xs:complexType name=\"" + name + "\">");
		this.xsd.append("<xs:sequence>");
		for (final Entry<String, String> entry : properties.entrySet()) {
			this.xsd.append("<xs:element name=\"" + entry.getKey() + "\" type=\"xs:" + entry.getValue() + "\"");
		}
		this.xsd.append("</xs:sequence>");
		this.xsd.append("</xs:complexType>");
	}

	private Interfaces parseNodeTypeInterfaces(Map<String, String> interfaces) {
		final Interfaces result = new Interfaces();
		for (final Entry<String, String> entry : interfaces.entrySet()) {
			final TInterface inf = new TInterface();
			inf.setName(entry.getKey());
			// TODO: YAMLmodel Interface Operations
			// inf.getOperation().add(e)
			result.getInterface().add(inf);
		}
		return result;
	}

	private DerivedFrom parseNodeTypeDerivedFrom(String derived_from) {
		final DerivedFrom result = new DerivedFrom();
		result.setTypeRef(new QName(derived_from));
		return result;
	}

	private CapabilityDefinitions parseNodeTypeCapabilities(Map<String, String> capabilities) {
		final CapabilityDefinitions result = new CapabilityDefinitions();
		for (final Entry<String, String> entr : capabilities.entrySet()) {
			final TCapabilityDefinition capdef = new TCapabilityDefinition();
			// TODO YAMLmodel NodeType Capabilities
			// capdef.setCapabilityType(value);
			// capdef.setConstraints(value);
			// capdef.setLowerBound(value);
			capdef.setName(entr.getKey());
			// capdef.setUpperBound(value);
			// capdef.getAny().add(e);
			// capdef.getDocumentation().add(e);
			// capdef.getOtherAttributes().put(key, value);
			result.getCapabilityDefinition().add(capdef);
		}
		return result;
	}

	private TDocumentation toDocumentation(String desc) {
		final TDocumentation docu = new TDocumentation();
		docu.getContent().add(desc);
		return docu;
	}

	private TNodeTemplate case_NodeTemplate(NodeTemplate elem) {
		final TNodeTemplate result = new TNodeTemplate();
		// result.setCapabilities(cap);
		// result.setDeploymentArtifacts(depa);
		result.setId(unique("nodetemplate"));
		// result.setMaxInstances(maxinst);
		// result.setMinInstances(mininst);
		result.setName(unique("Nodetemplate"));
		// result.setPolicies(poli);
		final TEntityTemplate.Properties prop = new TEntityTemplate.Properties();
		final QName type = new QName(elem.getType());
		final AnyMap properties = new AnyMap(elem.getProperties());
		prop.setAny(properties);
		result.setProperties(prop);
		// result.setPropertyConstraints(propconstr);
		// result.setRequirements(req);
		result.setType(type);
		// result.getAny().add(any);
		result.getDocumentation().add(toDocumentation(elem.getDescription()));
		// result.getOtherAttributes().put(name, attr)
		return result;
	}

	/**
	 * Adds a unique number to the prefix.
	 *
	 * @param prefix The prefix
	 * @return A unique String.
	 */
	private String unique(String prefix) {
		long id = this.uniqueID++;
		if (id < 0) {
			// Negative IDs are not pretty.
			this.uniqueID = 0;
			id = this.uniqueID;
		}
		return prefix + id;
	}

	/**
	 * If name contains multiple aliases the result is the first.
	 *
	 * @param name The name field of an XML element.
	 * @return A valid ID.
	 */
	private String name2id(String name) {
		return name.split(",")[0];
	}
}
