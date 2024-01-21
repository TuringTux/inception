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
package de.tudarmstadt.ukp.clarin.webanno.agreement;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.tudarmstadt.ukp.clarin.webanno.agreement.results.coding.FullCodingAgreementResult;
import de.tudarmstadt.ukp.clarin.webanno.agreement.results.unitizing.FullUnitizingAgreementResult;

public class BasicAgreementResult
    implements Serializable
{
    private static final long serialVersionUID = 5994827594084192896L;

    private final String type;
    private final String feature;
    private final boolean excludeIncomplete;
    private final List<String> casGroupIds = new ArrayList<>();

    private final List<Double> agreements = new ArrayList<>();
    private final Set<Object> categories = new LinkedHashSet<>();
    private final Map<String, Long> itemCounts = new HashMap<>();
    private final Map<String, Long> nonNullContentCounts = new HashMap<>();
    private final Map<String, Boolean> allNull = new HashMap<>();

    private boolean empty;

    private int incompleteSetsByPosition;
    private int incompleteSetsByLabel;
    private int pluralitySets;
    private int relevantSetCount;
    private int completeSetCount;

    public void merge(BasicAgreementResult aResult)
    {
        if (!type.equals(aResult.type)) {
            throw new IllegalArgumentException("All merged results must have the same type [" + type
                    + "] but encounterd [" + aResult.type + "]");
        }

        if (!feature.equals(aResult.feature)) {
            throw new IllegalArgumentException("All merged results must have the same feature ["
                    + feature + "] but encounterd [" + aResult.feature + "]");
        }

        if (excludeIncomplete != aResult.excludeIncomplete) {
            throw new IllegalArgumentException(
                    "All merged results must have the same excludeIncomplete [" + excludeIncomplete
                            + "] but encounterd [" + aResult.excludeIncomplete + "]");
        }

        if (!casGroupIds.equals(aResult.casGroupIds)) {
            throw new IllegalArgumentException("All merged results must have the same casGroupIds "
                    + casGroupIds + " but encounterd " + aResult.casGroupIds);
        }

        agreements.addAll(aResult.agreements);
        categories.addAll(aResult.categories);
        for (var e : aResult.itemCounts.entrySet()) {
            itemCounts.merge(e.getKey(), e.getValue(), Long::sum);
        }
        for (var e : aResult.nonNullContentCounts.entrySet()) {
            nonNullContentCounts.merge(e.getKey(), e.getValue(), Long::sum);
        }
        for (var e : aResult.allNull.entrySet()) {
            allNull.merge(e.getKey(), e.getValue(), Boolean::logicalOr);
        }

        empty |= aResult.empty;

        if (incompleteSetsByPosition >= 0 && aResult.incompleteSetsByPosition >= 0) {
            incompleteSetsByPosition += aResult.incompleteSetsByPosition;
        }

        if (incompleteSetsByLabel >= 0 && aResult.incompleteSetsByLabel >= 0) {
            incompleteSetsByLabel += aResult.incompleteSetsByLabel;
        }

        if (pluralitySets >= 0 && aResult.pluralitySets >= 0) {
            pluralitySets += aResult.pluralitySets;
        }

        if (relevantSetCount >= 0 && aResult.relevantSetCount >= 0) {
            relevantSetCount += aResult.relevantSetCount;
        }

        if (completeSetCount >= 0 && aResult.completeSetCount >= 0) {
            completeSetCount += aResult.completeSetCount;
        }
    }

    public static BasicAgreementResult of(Serializable aResult)
    {
        if (aResult instanceof FullCodingAgreementResult result) {
            return new BasicAgreementResult(result);
        }

        if (aResult instanceof FullUnitizingAgreementResult result) {
            return new BasicAgreementResult(result);
        }

        throw new IllegalArgumentException(
                "Unsupported result type: [" + aResult.getClass().getName() + "]");
    }

    public BasicAgreementResult(FullUnitizingAgreementResult aResult)
    {
        this((FullAgreementResult_ImplBase<?>) aResult);

        incompleteSetsByLabel = -1;
        incompleteSetsByPosition = -1;
        relevantSetCount = -1;
        completeSetCount = -1;
        pluralitySets = -1;
    }

    public BasicAgreementResult(FullCodingAgreementResult aResult)
    {
        this((FullAgreementResult_ImplBase<?>) aResult);

        incompleteSetsByLabel = aResult.getIncompleteSetsByLabel().size();
        incompleteSetsByPosition = aResult.getIncompleteSetsByPosition().size();
        pluralitySets = aResult.getPluralitySets().size();
        relevantSetCount = aResult.getRelevantSetCount();
        completeSetCount = aResult.getCompleteSetCount();
    }

    private BasicAgreementResult(FullAgreementResult_ImplBase<?> aResult)
    {
        type = aResult.getType();
        feature = aResult.getFeature();
        excludeIncomplete = isExcludeIncomplete();
        casGroupIds.addAll(aResult.casGroupIds);
        agreements.add(aResult.agreement);
        aResult.getCategories().forEach(categories::add);
        empty = aResult.isEmpty();

        for (var casGroupId : casGroupIds) {
            itemCounts.put(casGroupId, aResult.getItemCount(casGroupId));
            nonNullContentCounts.put(casGroupId, aResult.getNonNullCount(casGroupId));
            allNull.put(casGroupId, aResult.isAllNull(casGroupId));
        }
    }

    public List<String> getCasGroupIds()
    {
        return casGroupIds;
    }

    public double getAgreement()
    {
        return agreements.stream().mapToDouble(a -> a).average().orElse(Double.NaN);
    }

    public String getType()
    {
        return type;
    }

    public String getFeature()
    {
        return feature;
    }

    public boolean isExcludeIncomplete()
    {
        return excludeIncomplete;
    }

    public boolean isEmpty()
    {
        return empty;
    }

    public long getItemCount(String aRater)
    {
        return itemCounts.getOrDefault(aRater, 0l);
    }

    public int getCategoryCount()
    {
        return categories.size();
    }

    public Long getNonNullCount(String aRater)
    {
        return nonNullContentCounts.getOrDefault(aRater, 0l);
    }

    public boolean isAllNull(String aRater)
    {
        return allNull.getOrDefault(aRater, true);
    }

    public int getIncompleteSetsByPosition()
    {
        return incompleteSetsByPosition;
    }

    public int getIncompleteSetsByLabel()
    {
        return incompleteSetsByLabel;
    }

    public int getRelevantSetCount()
    {
        return relevantSetCount;
    }

    public int getCompleteSetCount()
    {
        return completeSetCount;
    }

    public int getPluralitySets()
    {
        return pluralitySets;
    }
}
