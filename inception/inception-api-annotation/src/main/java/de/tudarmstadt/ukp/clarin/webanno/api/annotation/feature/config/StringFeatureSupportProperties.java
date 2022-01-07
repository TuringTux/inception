/*
 * Licensed to the Technische Universität Darmstadt under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The Technische Universität Darmstadt 
 * licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.
 *  
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.tudarmstadt.ukp.clarin.webanno.api.annotation.feature.config;

public interface StringFeatureSupportProperties
{
    /**
     * If the tagset is larger than the threshold, a combo-box is used instead of a radio choice.
     */
    int getComboBoxThreshold();

    /**
     * If the tagset is larger than the threshold, an auto-complete field is used instead of a
     * standard combobox.
     */
    int getAutoCompleteThreshold();

    /**
     * When an auto-complete field is used, this determines the maximum number of items shown in the
     * dropdown menu.
     */
    int getAutoCompleteMaxResults();
}