/*                                                                                                                                                                                                                                                                         
 * Licensed to the Apache Software Foundation (ASF) under one                                                                                                                                                                                                              
 * or more contributor license agreements.  See the NOTICE file                                                                                                                                                                                                            
 * distributed with this work for additional information                                                                                                                                                                                                                   
 * regarding copyright ownership.  The ASF licenses this file                                                                                                                                                                                                              
 * to you under the Apache License, Version 2.0 (the                                                                                                                                                                                                                       
 * "License"); you may not use this file except in compliance                                                                                                                                                                                                              
 * with the License.  You may obtain a copy of the License at                                                                                                                                                                                                              
 *                                                                                                                                                                                                                                                                         
 * http://www.apache.org/licenses/LICENSE-2.0                                                                                                                                                                                                                              
 *                                                                                                                                                                                                                                                                         
 * Unless required by applicable law or agreed to in writing,                                                                                                                                                                                                              
 * software distributed under the License is distributed on an                                                                                                                                                                                                             
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY                                                                                                                                                                                                                  
 * KIND, either express or implied.  See the License for the                                                                                                                                                                                                               
 * specific language governing permissions and limitations                                                                                                                                                                                                                 
 * under the License.                                                                                                                                                                                                                                                      
 */
package org.apache.tinkerpop.gremlin.process.computer.bulkloading;

import org.apache.tinkerpop.gremlin.process.computer.bulkloading.IncrementalBulkLoader;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.VertexProperty;
import org.apache.tinkerpop.gremlin.structure.Property;

import java.lang.String;

/**                                                                                                                                                                                                                                                                        
 * @author Daniel Kuppitz (http://gremlin.guru)                                                                                                                                                                                                                        
 */
public class TitanBulkLoader extends IncrementalBulkLoader {

    @Override
    public VertexProperty getOrCreateVertexProperty(final VertexProperty<?> property, final Vertex vertex, final Graph graph, final GraphTraversalSource g) {
        if (property.key().contains(":")) {
            final String[] parts = property.key().split(":");
            final String cardinality = parts[0];
            final String propertyKey = parts[1];
            if (cardinality.equals("set")) {
                final VertexProperty<?> vp = vertex.property(VertexProperty.Cardinality.set, propertyKey, property.value());
                property.properties().forEachRemaining(metaProperty -> {
                        vp.property(metaProperty.key(), metaProperty.value());
                    });
                return vp;
            } else if (cardinality.equals("list")) {
                final VertexProperty<?> vp = vertex.property(VertexProperty.Cardinality.list, propertyKey, property.value());
                property.properties().forEachRemaining(metaProperty -> {
                        vp.property(metaProperty.key(), metaProperty.value());
                    });
                return vp;
            }
        }
        return super.getOrCreateVertexProperty(property, vertex, graph, g);
    }
}
