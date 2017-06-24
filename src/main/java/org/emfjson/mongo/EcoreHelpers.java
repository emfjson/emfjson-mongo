package org.emfjson.mongo;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;

public class EcoreHelpers {

	public static String uriOf(EClass type) {
		return type != null ? EcoreUtil.getURI(type).toString(): null;
	}

	public static String uriOf(Resource resource) {
		return (resource != null && resource.getURI() != null) ? resource.getURI().toString(): null;
	}

}
