import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;


public class SetOperationsUtility 
{
	public static boolean isNumeric(Map<String,ArrayList<Integer>> attributeValueMapWithoutDupicates)
	{
		Set<String> attributeValueMapKeys = attributeValueMapWithoutDupicates.keySet();
		Iterator<String> attributeValueIterator = attributeValueMapKeys.iterator();
		String attributeValue = attributeValueIterator.next();
		return attributeValue.matches("-?\\d+(\\.\\d+)?");  //match a number with optional '-' and decimal.
	}
	
	public static Set<Integer> intersection(Set<Integer> set1, Set<Integer> set2)
	{
		Set<Integer> intersectionSet = new LinkedHashSet<Integer>();
		for (Integer i:set1)
			for(Integer j:set2)
				if(i==j)
					intersectionSet.add(j);
		
		return intersectionSet;
	}
	
	public static boolean isSubset(Set<Integer> subSet, Set<Integer> superSet)
	{
		if(subSet.size()>0)
		{
			if(superSet.containsAll(subSet))
				return true;
			else
				return false;
		}
		else
			return false;
	}
	
	public static Set<Integer> union(Set<Integer> set1,Set<Integer> set2)
	{
		Set<Integer> unionSet = new LinkedHashSet<Integer>();
		unionSet.addAll(set1);
		unionSet.addAll(set2);
		return unionSet;
	}
	
	public static Set<Integer> subtractSetAFromSetB (Set<Integer> setA,Set<Integer> setB)
	{
		
		Set<Integer> subtractedSet= new LinkedHashSet<Integer>();
		subtractedSet.addAll(setB);
		for(Integer w:setA)
			if(setB.contains(w))
				subtractedSet.remove(w);		
		return subtractedSet;
		
	}

	public static boolean areDisjointSets(AttributeData selectedAttribute,AttributeData tempAttributeValuePair) 
	{
		double selectedRange1 = selectedAttribute.getRange1();
		double selectedRange2 = selectedAttribute.getRange2();
		double tempRange1 = tempAttributeValuePair.getRange1();
		double tempRange2 = tempAttributeValuePair.getRange2();
		
		if(tempRange2<=selectedRange1 || tempRange1>=selectedRange2)
			return true;
		else 
			return false;
	}
}
