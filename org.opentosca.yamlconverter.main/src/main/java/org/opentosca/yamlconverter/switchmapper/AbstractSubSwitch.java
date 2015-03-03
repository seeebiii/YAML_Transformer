package org.opentosca.yamlconverter.switchmapper;

import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

import org.opentosca.model.tosca.Definitions;
import org.opentosca.model.tosca.TDocumentation;
import org.opentosca.model.tosca.TEntityType.DerivedFrom;
import org.opentosca.model.tosca.TEntityType.PropertiesDefinition;
import org.opentosca.model.tosca.TExtensibleElements;
import org.opentosca.model.tosca.TInterface;
import org.opentosca.model.tosca.TOperation;
import org.opentosca.model.tosca.TTopologyTemplate;
import org.opentosca.yamlconverter.main.utils.AnyMap;
import org.opentosca.yamlconverter.yamlmodel.yaml.element.PropertyDefinition;
import org.opentosca.yamlconverter.yamlmodel.yaml.element.ServiceTemplate;

import com.esotericsoftware.yamlbeans.YamlException;
import com.esotericsoftware.yamlbeans.YamlWriter;

public abstract class AbstractSubSwitch implements ISubSwitch {
	private final Yaml2XmlSwitch parent;
	private TTopologyTemplate topologyCache;

	public AbstractSubSwitch(Yaml2XmlSwitch parentSwitch) {
		this.parent = parentSwitch;
	}

	protected Definitions getDefinitions() {
		return this.parent.getToscaResult();
	}

	protected ServiceTemplate getServiceTemplate() {
		return this.parent.getServiceTemplate();
	}

	protected TTopologyTemplate getTopologyTemplate() {
		if (this.topologyCache == null) {
			for (final TExtensibleElements elem : getDefinitions().getServiceTemplateOrNodeTypeOrNodeTypeImplementation()) {
				if (elem instanceof TTopologyTemplate) {
					this.topologyCache = (TTopologyTemplate) elem;
					break;
				}
			}
		}
		return this.topologyCache;
	}

	protected TDocumentation toDocumentation(String desc) {
		final TDocumentation docu = new TDocumentation();
		docu.getContent().add(desc);
		return docu;
	}

	/**
	 * If name contains multiple aliases the result is the first.
	 *
	 * @param name The name field of an XML element.
	 * @return A valid ID.
	 */
	protected String name2id(String name) {
		return name.split(",")[0];
	}

	protected DerivedFrom parseDerivedFrom(String derived_from) {
		final DerivedFrom result = new DerivedFrom();
		result.setTypeRef(new QName(derived_from));
		return result;
	}

	protected PropertiesDefinition parsePropertiesDefinition(Map<String, PropertyDefinition> properties, String typename) {
		final PropertiesDefinition result = new PropertiesDefinition();
		// TODO: setType()?!
		result.setElement(new QName(Yaml2XmlSwitch.TYPESNS, typename + "Properties", "types"));
		generateTypeXSD(properties, typename + "Properties");
		return result;
	}

	private void generateTypeXSD(Map<String, PropertyDefinition> properties, String name) {
		this.parent.getXSDStringBuilder().append("<xs:complexType name=\"" + name + "\">\n");
		this.parent.getXSDStringBuilder().append("<xs:sequence>\n");
		for (final Entry<String, PropertyDefinition> entry : properties.entrySet()) {
			this.parent.getXSDStringBuilder().append(
					"<xs:element name=\"" + entry.getKey() + "\" type=\"xs:" + entry.getValue().getType() + "\" />\n");
		}
		this.parent.getXSDStringBuilder().append("</xs:sequence>\n");
		this.parent.getXSDStringBuilder().append("</xs:complexType>\n");
	}

	protected JAXBElement<AnyMap> getAnyMapForProperties(final Map<String, Object> customMap, final String nodename) {
		final AnyMap properties = new AnyMap(parseProperties(customMap));
		return new JAXBElement<AnyMap>(new QName(Yaml2XmlSwitch.TYPESNS, nodename + "Properties", "types"), AnyMap.class, properties);
	}

	@SuppressWarnings("unchecked")
	private Map<String, String> parseProperties(Map<String, Object> properties) {
		final Map<String, String> result = new HashMap<String, String>();
		for (final Entry<String, Object> entry : properties.entrySet()) {
			String value = "";
			if (isGetter(entry.getValue())) {
				value = parseGetter((Map<String, Object>) entry.getValue());
			} else {
				value = (String) entry.getValue();
			}
			result.put(entry.getKey(), value);
		}
		return result;
	}

	/**
	 * Checks whether the Object is a Getter or just a normal property.
	 *
	 * @param value
	 * @return true if getter, false if property
	 */
	private boolean isGetter(Object value) {
		if (value instanceof Map<?, ?>) {
			return true;
		}
		return false;
	}

	protected TInterface getInterfaceWithOperations(Entry<String, Map<String, Map<String, String>>> entry) {
		final TInterface inf = new TInterface();
		inf.setName(entry.getKey());
		// TODO: is this right?!
		for (final Entry<String, Map<String, String>> op : entry.getValue().entrySet()) {
			final TOperation top = new TOperation();
			top.setName(op.getKey());
			// value contains keys "implementation" and "description" eventually
			// TODO: how to use implementation name??
			inf.getOperation().add(top);
		}
		return inf;
	}

	private String parseGetter(Map<String, Object> getterMap) {
		for (final Entry<String, Object> getter : getterMap.entrySet()) {
			switch (getter.getKey()) {
			case "get_input":
				final String inputvar = (String) getter.getValue();
				if (this.parent.getInputs().containsKey(inputvar)) {
					return this.parent.getInputs().get(inputvar);
				}
				if (getServiceTemplate().getInputs().containsKey(inputvar)) {
					if (getServiceTemplate().getInputs().get(inputvar).getDefault() != null
							&& !getServiceTemplate().getInputs().get(inputvar).getDefault().isEmpty()) {
						return getServiceTemplate().getInputs().get(inputvar).getDefault();
					}
				}
				// TODO: *Type-defaults
				return Yaml2XmlSwitch.DEFAULT_USER_INPUT;
			case "get_property":
				@SuppressWarnings("unchecked")
				final List<String> list = (List<String>) getter.getValue();
				final String template = list.get(0);
				final String property = list.get(1);
				if (getServiceTemplate().getNode_templates().containsKey(template)) {
					if (getServiceTemplate().getNode_templates().get(template).getProperties().containsKey(property)) {
						return (String) getServiceTemplate().getNode_templates().get(template).getProperties().get(property);
					}
				}
			case "get_ref_property":
				return Yaml2XmlSwitch.DEFAULT_USER_INPUT;
			default:
				final String result = serializeYAML(getterMap);
				if (result != null) {
					return result;
				} else {
					return Yaml2XmlSwitch.DEFAULT_USER_INPUT;
				}
			}
		}
		return "";
	}

	private String serializeYAML(Map<String, Object> getterMap) {
		final Writer output = new StringWriter();
		final YamlWriter writer = new YamlWriter(output);
		try {
			writer.write(getterMap);
			writer.close();
		} catch (final YamlException e) {
			return null;
		}
		return output.toString();
	}

	@Override
	public abstract void process();
}
