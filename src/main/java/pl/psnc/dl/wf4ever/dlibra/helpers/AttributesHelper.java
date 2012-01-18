package pl.psnc.dl.wf4ever.dlibra.helpers;

import java.net.URI;
import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import pl.psnc.dlibra.common.CollectionResult;
import pl.psnc.dlibra.common.DLObject;
import pl.psnc.dlibra.common.OutputFilter;
import pl.psnc.dlibra.metadata.EditionId;
import pl.psnc.dlibra.metadata.Language;
import pl.psnc.dlibra.metadata.attributes.AbstractAttributeValue;
import pl.psnc.dlibra.metadata.attributes.Attribute;
import pl.psnc.dlibra.metadata.attributes.AttributeFilter;
import pl.psnc.dlibra.metadata.attributes.AttributeId;
import pl.psnc.dlibra.metadata.attributes.AttributeInfo;
import pl.psnc.dlibra.metadata.attributes.AttributeManager;
import pl.psnc.dlibra.metadata.attributes.AttributeValue;
import pl.psnc.dlibra.metadata.attributes.AttributeValueFilter;
import pl.psnc.dlibra.metadata.attributes.AttributeValueId;
import pl.psnc.dlibra.metadata.attributes.AttributeValueManager;
import pl.psnc.dlibra.metadata.attributes.AttributeValueSet;
import pl.psnc.dlibra.service.AccessDeniedException;
import pl.psnc.dlibra.service.DLibraException;
import pl.psnc.dlibra.service.DuplicatedValueException;
import pl.psnc.dlibra.service.IdNotFoundException;

import com.google.common.collect.Multimap;

public class AttributesHelper
{

	private final static Logger logger = Logger.getLogger(AttributesHelper.class);

	public static final String ATTRIBUTE_LANGUAGE = Language.UNIVERSAL;

	public static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss z");

	private final DLibraDataSource dl;


	public AttributesHelper(DLibraDataSource dl)
	{
		this.dl = dl;
	}


	public void storeAttributes(String workspaceId, String researchObjectId, String versionId,
			Multimap<URI, Object> attributes)
		throws RemoteException, IdNotFoundException, DLibraException
	{
		AttributeValueSet avs = getAttributeValueSet(workspaceId, researchObjectId, versionId);
		for (URI uri : attributes.keySet()) {
			updateAttribute(avs, uri, attributes.get(uri));
		}
		commitAttributeValueSet(avs);
	}


	private AttributeValueSet getAttributeValueSet(String workspaceId, String researchObjectId, String versionId)
		throws RemoteException, DLibraException, IdNotFoundException
	{
		EditionId editionId = dl.getEditionHelper().getLastEditionId(researchObjectId, versionId);

		AttributeValueSet avs = dl.getMetadataServer().getElementMetadataManager()
				.getAttributeValueSet(editionId, AttributeValue.AV_ASSOC_ALL);
		return avs;
	}


	private void commitAttributeValueSet(AttributeValueSet avs)
		throws RemoteException, IdNotFoundException, AccessDeniedException, DLibraException
	{
		dl.getMetadataServer().getElementMetadataManager().setAttributeValueSet(avs);
	}


	private void updateAttribute(AttributeValueSet avs, URI attributeRdfName, Collection<Object> values)
		throws IdNotFoundException, RemoteException, DLibraException
	{
		if (values.isEmpty()) {
			logger.warn(String.format("Ignoring empty value for attribute %s", attributeRdfName));
			return;
		}

		// find the attribute
		AttributeInfo attributeInfo = getAttribute(attributeRdfName);

		// a new list, which will replace all previous values
		List<AbstractAttributeValue> attValues = new ArrayList<AbstractAttributeValue>();

		for (Object o : values) {
			// create attribute value
			AttributeValue attValue = new AttributeValue(null);
			attValue.setAttributeId(attributeInfo.getId());
			// TODO not always toString, for example Calendar needs to be nicely printed
			String s = null;
			if (o instanceof Calendar) {
				s = SDF.format(((Calendar) o).getTime());
			}
			else {
				s = o.toString();
			}
			attValue.setValue(s);
			attValue.setLanguageName(ATTRIBUTE_LANGUAGE);
			attValue = createAttributeValue(attValue);

			// add to list
			attValues.add(attValue);
		}

		// update attribute value set
		avs.setDirectAttributeValues(attributeInfo.getId(), ATTRIBUTE_LANGUAGE, attValues);

		logger.debug(String.format("Updated attribute %s (%d) with %d values such as %s", attributeInfo.getRDFName(),
			attributeInfo.getId().getId(), values.size(), values.iterator().next().toString()));
	}


	/**
	 * @param attributeRdfName
	 * @return
	 * @throws IdNotFoundException
	 * @throws RemoteException
	 * @throws DLibraException
	 */
	private AttributeInfo getAttribute(URI attributeRdfName)
		throws IdNotFoundException, RemoteException, DLibraException
	{
		AttributeInfo attributeInfo = findExistingAttribute(attributeRdfName.toString());
		if (attributeInfo == null) {
			String name;
			if (attributeRdfName.getFragment() != null)
				name = attributeRdfName.getFragment();
			else
				name = attributeRdfName.resolve(".").relativize(attributeRdfName).toString();
			attributeInfo = createAttribute(attributeRdfName.toString(), name);
		}
		return attributeInfo;
	}


	/**
	 * @param attributeRdfName
	 * @return
	 * @throws IdNotFoundException
	 * @throws RemoteException
	 * @throws DLibraException
	 */
	private AttributeInfo findExistingAttribute(String attributeRdfName)
		throws IdNotFoundException, RemoteException, DLibraException
	{
		CollectionResult result = dl
				.getMetadataServer()
				.getAttributeManager()
				.getObjects(new AttributeFilter((AttributeId) null).setRDFNames(Arrays.asList(attributeRdfName)),
					new OutputFilter(AttributeInfo.class));
		if (result.getResultsCount() > 1) {
			logger.debug(String.format("Found %d attributes with RDF name '%s'", result.getResultsCount(),
				attributeRdfName));
			return (AttributeInfo) result.getResultInfos().iterator().next();
		}
		else if (result.getResultsCount() == 1) {

			return (AttributeInfo) result.getResultInfo();
		}
		return null;
	}


	private AttributeInfo createAttribute(String attributeRdfName, String name)
		throws UnsupportedOperationException, IdNotFoundException, RemoteException, DLibraException
	{
		// this is to use localName of properties
		attributeRdfName = name;
		AttributeManager attributeManager = dl.getMetadataServer().getAttributeManager();
		Attribute att = new Attribute(null);
		att.setLanguageName("pl");
		att.setName(name);
		att.setLanguageName("en");
		att.setName(name);
		att.setLanguageName(ATTRIBUTE_LANGUAGE);
		att.setRDFName(attributeRdfName);
		AttributeId id = attributeManager.addAttribute(att);
		att.setId(id);
		logger.debug(String.format("Created a new attribute with RDF name %s", att.getRDFName()));
		return (AttributeInfo) att.getInfo();
	}


	/**
	 * Adds attribute value specified by text value. Chooses one from system or creates a
	 * new one if such value does not exist.
	 * 
	 * @param value
	 *            Text specifying value.
	 * @return attribute value
	 * @throws RemoteException
	 *             in case remote server exception occurred.
	 * @throws IdNotFoundException
	 *             in case there was a problem with identifiers.
	 * @throws DLibraException
	 * @throws RemoteException
	 * @throws OperationFailedException
	 * @throws DuplicatedValueException
	 *             when new group cannot be created because of name conflict (may occur
	 *             when concurrent modifications are performed).
	 * @throws AccessDeniedException
	 *             when there is no access to perform adding new group operation.
	 */
	private AttributeValue createAttributeValue(final AttributeValue value)
		throws RemoteException, DLibraException
	{
		AttributeValueManager attributeValueManager = dl.getMetadataServer().getAttributeValueManager();
		List<AttributeValue> groupsWithValue = new ArrayList<AttributeValue>();
		for (DLObject obj : attributeValueManager.getObjects(
			new AttributeValueFilter(value.getAttributeId()).setValue(value.getValue(), true).setLanguageName(
				value.getLanguageName()), new OutputFilter(AttributeValue.class)).getResults())
			groupsWithValue.add((AttributeValue) obj);
		if (groupsWithValue.isEmpty()) {
			logger.debug("No groups with value " + value);
			AttributeValueId id = attributeValueManager.addAttributeValue(value);
			value.setId(id);
			return value;
		}
		else if (groupsWithValue.size() == 1) {
			AttributeValue group = groupsWithValue.get(0);
			if (group != null) {
				return getAttributeValueFromGroup(value.getValue(), group, value.getLanguageName());
			}
		}
		else {
			// return getGroupForValue(value, groupsWithValue);
		}
		return null;
	}


	private AttributeValue getAttributeValueFromGroup(String value, AttributeValue group, String selLang)
		throws RemoteException, IdNotFoundException, DLibraException

	{
		Collection<DLObject> groupValues = dl.getMetadataServer().getAttributeValueManager()
				.getObjects(new AttributeValueFilter(null, group.getId()), new OutputFilter(AttributeValue.class))
				.getResults();
		AttributeValue foundValue = null;
		boolean eqIgnCase = false;
		for (Iterator<DLObject> iter = groupValues.iterator(); iter.hasNext();) {
			AttributeValue element = (AttributeValue) iter.next();
			element.setLanguageName(selLang);
			if (element.getValue().equalsIgnoreCase(value)) {
				eqIgnCase = true;
			}
			if (element.getValue().equals(value)) {
				foundValue = element;
				break;
			}
		}
		if (foundValue != null) {
			return foundValue;
		}
		if (eqIgnCase) {
			// FIXME: what to throw?
		}
		throw new IdNotFoundException("Could not find value in group. Value: " + value + " Group id: "
				+ group.getBaseId() + " Language name: " + selLang);
	}

}
