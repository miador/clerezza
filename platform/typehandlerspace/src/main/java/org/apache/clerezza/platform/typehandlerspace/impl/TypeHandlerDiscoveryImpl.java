/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.clerezza.platform.typehandlerspace.impl;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.References;
import org.apache.felix.scr.annotations.Service;
import org.apache.clerezza.platform.typehandlerspace.SupportedTypes;
import org.apache.clerezza.platform.typehandlerspace.TypeHandlerDiscovery;
import org.apache.clerezza.platform.typepriority.TypePrioritizer;
import org.apache.clerezza.commons.rdf.IRI;
import org.apache.clerezza.rdf.ontologies.RDFS;
import org.apache.felix.scr.annotations.ReferencePolicy;

/**
 * @author rbn
 */
@Component
@Service(value=TypeHandlerDiscovery.class)
@References({
    @Reference(name="typeHandler",
        cardinality=ReferenceCardinality.OPTIONAL_MULTIPLE,
        referenceInterface=Object.class,
        target="(org.apache.clerezza.platform.typehandler=true)",
        policy=ReferencePolicy.DYNAMIC)})
public class TypeHandlerDiscoveryImpl implements TypeHandlerDiscovery {


    @Reference
    private TypePrioritizer typePrioritizer;

    private final Map<IRI, Object> typeHandlerMap = Collections.synchronizedMap(new HashMap<IRI, Object>());
    
    protected void bindTypeHandler(Object typeHandler) {
        SupportedTypes supportedTypes = typeHandler.getClass()
                .getAnnotation(SupportedTypes.class);
        if (supportedTypes == null) {
            return;
        }
        for (String typeUriString : supportedTypes.types()) {
            IRI typeUri = new IRI(typeUriString);
            typeHandlerMap.put(typeUri, typeHandler);
        }
    }
        
    protected void unbindTypeHandler(Object typeHandler) {
        Iterator<IRI> keys = typeHandlerMap.keySet().iterator();
        Set<IRI> toRemove = new HashSet<IRI>(typeHandlerMap.size());
        synchronized(typeHandlerMap) {
            while (keys.hasNext()) {
                IRI uriRef = keys.next();
                if(typeHandlerMap.get(uriRef) == typeHandler) {
                    toRemove.add(uriRef);
                }
            }
        }
        keys = toRemove.iterator();
        while (keys.hasNext()) {
            typeHandlerMap.remove(keys.next());
        }
    }

    @Override
    public Object getTypeHandler(final Set<IRI> types) {
        return AccessController.doPrivileged(new PrivilegedAction<Object>() {

            @Override
            public Object run() {
                Iterator<IRI> prioritizedTypes = typePrioritizer.iterate(types);
                while (prioritizedTypes.hasNext()) {
                    Object result = typeHandlerMap.get(prioritizedTypes.next());
                    if (result != null) {
                        return result;
                    }
                }
                return typeHandlerMap.get(RDFS.Resource);
            }
        });        
    }

}
