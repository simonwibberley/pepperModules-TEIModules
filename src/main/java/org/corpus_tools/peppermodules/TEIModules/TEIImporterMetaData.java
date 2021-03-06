/**
 * Copyright 2009 Humboldt-Universität zu Berlin, INRIA.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 */
package org.corpus_tools.peppermodules.TEIModules;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.corpus_tools.salt.common.SDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TEIImporterMetaData {
	private static Logger logger = LoggerFactory.getLogger(TEIImporterMetaData.class);

	/**
	 * stack saving the names of tags
	 */
	private Stack<String> pathStack = new Stack<String>();

	/**
	 * map with xpaths as keys and text between tags as values
	 */
	private Map<String, String> XPathMap = null;

	/**
	 * set containing all existing xpaths
	 */
	private Set<String> PathSet = new HashSet<String>();

	/**
	 * map with xpaths as keys and customized(or hardcoded) SMetaAnnotations as
	 * values
	 */
	private Map<String, String> MappingMap = null;

	/**
	 * gets MappingMap
	 * 
	 * @return returns MappingMap
	 */
	public Map<String, String> getMappingMap() {
		return MappingMap;
	}

	/**
	 * gets pathStack
	 */
	public Stack<String> getPathStack() {
		return pathStack;
	}

	/**
	 * sets pathStack
	 */
	public void setPathStack(Stack<String> pathStack) {
		this.pathStack = pathStack;
	}

	/**
	 * gets XPathmap
	 */
	public Map<String, String> getXPathMap() {
		return XPathMap;
	}

	/**
	 * sets XPathmap
	 */
	public void setXPathMap(Map<String, String> xPathMap) {
		XPathMap = xPathMap;
	}

	/**
	 * gets PathSet
	 */
	public Set<String> getPathSet() {
		return PathSet;
	}

	/**
	 * sets PathSet
	 */
	public void setPathSet(Set<String> pathSet) {
		PathSet = pathSet;
	}

	/**
	 * constructor that also adds default mappings
	 */
	public TEIImporterMetaData() {
		MappingMap = new Hashtable<>();
		// add default mappings to MappingMap
		MappingMap.put("/fileDesc/titleStmt/author", "author");
		MappingMap.put("/fileDesc/titleStmt/title", "title");
	}

	/**
	 * pushes tag-name to pathStack
	 * 
	 * @param tag
	 *            tag-name
	 */
	public void push(String tag) {
		boolean run = true;
		for (int i = 1; run; i++) {
			if (!PathSet.contains(getcurrentpath() + "/" + tag + "[" + i + "]")) {
				pathStack.push(tag + "[" + i + "]");
				run = false;
			}
		}
		PathSet.add(getcurrentpath());
	}

	/**
	 * pops pathStack
	 * 
	 * @return name of popped tag
	 */
	public String pop() {
		return (pathStack.pop());
	}

	/**
	 * returns current path
	 * 
	 * @return current path of metadata
	 */
	public String getcurrentpath() {
		String temp = "/";
		for (int i = 0; (i < pathStack.size()); i++) {
			temp = temp + pathStack.elementAt(i) + "/";
		}
		if (temp.length() > 0) {
			temp = temp.substring(0, temp.length() - 1);
		}
		return (temp);
	}

	/**
	 * pushes text inside tags to XPathMap and creates empty Hashtable if
	 * XPathMap == null
	 * 
	 * @param value
	 *            that is pushed to XPathMap
	 */
	public void push_to_XPathMap(String value) {
		if (XPathMap == null) {
			XPathMap = new Hashtable<>();
		}
		XPathMap.put(getcurrentpath(), value);
	}

	/**
	 * pushes attributes to XPathMap and creates empty Hashtable if XPathMap ==
	 * null
	 * 
	 * @param attribute
	 *            relevant attribute to be pushed
	 * @param value
	 *            that is pushed to XPathMap
	 */
	public void push_attribute_XPathMap(String attribute, String value) {
		if (XPathMap == null) {
			XPathMap = new Hashtable<>();
		}
		XPathMap.put(getcurrentpath() + "/@" + attribute, value);
	}

	/**
	 * merges hardcoded mappings with those set by the user
	 * 
	 * @param overwritingMap
	 *            map from TEIImporterProperties
	 * @return returns the united map
	 */
	public Map<String, String> uniteMappings(Map<String, String> overwritingMap) {
		MappingMap.putAll(overwritingMap);
		return (MappingMap);
	}

	/**
	 * adds and manipulates the metadata stored in a map
	 * 
	 * @param MetaMap
	 *            map containing metadata before mappings applied
	 * @param customMap
	 *            map containing the mappings
	 * @param delRedundant
	 *            boolean for the decision to delete or keep redundant metadata
	 *            after applying mappings
	 * @return returns a map with applied mappings
	 */
	public Map<String, String> mapToXpathMap(Map<String, String> MetaMap, Map<String, String> customMap, Boolean delRedundant) {
		Set<String> customSet = customMap.keySet();
		Iterator<String> it = customSet.iterator();
		while (it.hasNext()) {
			String oldKey = it.next();
			String value;
			if (MetaMap.containsKey(oldKey)) {
				value = MetaMap.get(oldKey);
				String newKey = customMap.get(oldKey);
				MetaMap.put(newKey, value);
			}
		}
		if (delRedundant) {
			it = customSet.iterator();
			while (it.hasNext()) {
				String oldKey = it.next();
				MetaMap.remove(oldKey);
			}
		}
		return MetaMap;
	}

	/**
	 * This method removes the "1" from the pseudo-Xpath
	 * 
	 * @param map
	 *            the map to to be corrected
	 * @return the corrected map
	 */
	public Map<String, String> remove_ones(Map<String, String> map) {
		Map<String, String> newMap = new Hashtable<>();
		Set<String> keySet = XPathMap.keySet();
		for (String s : keySet) {
			String t = s.replace("[1]", "");
			String tempvalue = XPathMap.get(s);
			newMap.put(t, tempvalue);
		}
		return newMap;
	}

	/**
	 * adds SMetaAnnotations to SDocument(not SDocGraph!)
	 * 
	 * @param sdoc
	 *            SDocument
	 * @param map
	 *            contains mappings from xpath to customized annotation
	 */
	public void add_to_SDoc(SDocument sdoc, Map<String, String> map, Boolean lastPartOnly, Set<String> exclude) {
		Set<String> keySet = map.keySet();
		Iterator<String> it = keySet.iterator();
		while (it.hasNext()) {
			String tempkey = it.next();
			String tempvalue = map.get(tempkey);
			if (lastPartOnly) {
				String[] tempArray = tempkey.split("/");
				int len = tempArray.length;
				tempkey = tempArray[len - 1];
				tempkey = tempkey.replace("@", "");
			}
			if (tempvalue.length() > 0) {
				// create cleaned key, which does not contain positions of
				// elements, in case a position less xpath is given as property
				// to remove all occurences of the determined element
				String cleanedKey = tempkey.replaceAll("\\[\\d\\]", "");

				if ((!exclude.contains(tempkey)) && (!exclude.contains(cleanedKey))) {
					if (sdoc.getMetaAnnotation(tempkey) == null) {
						sdoc.createMetaAnnotation(null, tempkey, tempvalue);
					} else {
						logger.warn("You try to add a metadatum using a key for the second time. This second one will be ignored!");
					}
				}
			}
		}
	}
}