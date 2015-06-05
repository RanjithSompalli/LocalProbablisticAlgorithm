import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;



public class LocalProbablisticApproximationAlgorithm 
{
	static double alphaParameter = -1.0; 
	static ArrayList<AttributeData> attributeValuePairs;
	static ArrayList<AttributeData> conceptValues = new ArrayList<AttributeData>();
	static int mazSubsetSize = 0;
	static ArrayList<String> finalRules;
	
	public static void main(String args[]) throws Exception
	{
		String inputFileName=null;
		String outputFileName=null;
		boolean t = true;
		if(t)
		{
			Scanner scanner = new Scanner(System.in); 
			boolean file_exists=false;
			boolean alphaValueValid = false;
			while(!file_exists)
			{	
				System.out.println("Please enter the name of your input file:: ");
				inputFileName = scanner.next();
				File file= new File("input//"+inputFileName);
				file_exists = file.exists();
				if(!file_exists)
					System.err.println("No such file exists. Please re enter the file name!! \n ");
			}
			while(!alphaValueValid)
			{
				System.out.println("Please enter the value of the alpha parameter (0<= alphaValue <=1):: ");
				alphaParameter = scanner.nextDouble();
				if(alphaParameter >= 0.0 && alphaParameter <= 1.0)
					alphaValueValid = true;
				if(!alphaValueValid)
					System.err.println("Alpha Parameter Value Invalid. Please enter a valid value!! \n ");
			}
			System.out.println("Please Enter the Output file name (rules will be stored to this file):");
			outputFileName = scanner.next();
			scanner.close();
		}
		
		FileUtility utility = new FileUtility();	
		try
		{
			attributeValuePairs = utility.readInputFileData("input//"+inputFileName);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		if(attributeValuePairs != null)
		{
			finalRules =  implementAlgorithm(attributeValuePairs);
			System.out.println("printing the output to the file....");
			utility.writeRulesToFile("output//"+outputFileName,finalRules);
			System.out.println("Algorithm Succesfully Implemented. Rules are stored in output//"+outputFileName+ " file"); 
		}
		else
		{
			throw new Exception("Error Encountered While Evaluating Attribute Value pairs::::");
		}
		
	}	
	
	//Algorithm Implementation based on attribute value pairs
	private static ArrayList<String> implementAlgorithm(ArrayList<AttributeData> attributeValuePairs)
	{
		//Seperate the concept values from attribute value pairs.
		for(AttributeData data:attributeValuePairs)
		{
			if(data.getAttributeType().equalsIgnoreCase("concept"))
			{
				conceptValues.add(data);
			}
		}
		for(AttributeData concept: conceptValues)
		{
			attributeValuePairs.remove(concept);
		}
		
		//For each concept determine the rules
		ArrayList<String> finalRules_local = new ArrayList<String>();
		for(AttributeData concept: conceptValues)
		{
				finalRules_local.addAll(determineRulesforConcept(concept,attributeValuePairs));
		}
		return finalRules_local;
	}

	private static ArrayList<String> determineRulesforConcept(AttributeData concept,ArrayList<AttributeData> attributeValuePairs)
	{
		System.out.println("Determining Rules for:"+concept.getAttributeName());
		
		// initially assign the minimum cardinality to highest possible cardinality
		int minimumCardinality = 0;
		for(AttributeData data:attributeValuePairs)
			if(data.getCardinality()>minimumCardinality)
				minimumCardinality = data.getCardinality();

		Set<Integer> X = new LinkedHashSet<Integer>();
		X.addAll(concept.getValues());
		Set<Integer> G = new LinkedHashSet<Integer>();
		G.addAll(X);
		Set<Integer> D = new LinkedHashSet<Integer>();
		D.addAll(X);
		ArrayList<ArrayList<AttributeData>> rules = new ArrayList<ArrayList<AttributeData>>(); //Fat T
		ArrayList<ArrayList<AttributeData>> junk = new ArrayList<ArrayList<AttributeData>>(); //Fat J		
		Set<Integer> globalBoxT = new LinkedHashSet<Integer>();
		
		while(G.size()>0)
		{
			//Keep the Actual Attribute value pairs as is and operate on a temporary Attribute Value pairs
			ArrayList<AttributeData> tempAttributeValuePairs = new ArrayList<AttributeData>();
			tempAttributeValuePairs.addAll(attributeValuePairs);
			
			ArrayList<AttributeData> T = new ArrayList<AttributeData>(); //T
			ArrayList<AttributeData> TSymbolic = new ArrayList<AttributeData>(); //Ts
			ArrayList<AttributeData> TNumeric = new ArrayList<AttributeData>(); //Tn
			ArrayList<Map<AttributeData,Set<Integer>>> T_G = new ArrayList<Map<AttributeData,Set<Integer>>>(); //T(G)
			Set<Integer> boxT = new LinkedHashSet<Integer>(); //[T] 
			//Initially evaluate T_G once before looping
			determineT_G(T_G,tempAttributeValuePairs,G);
			if(T_G.size()==0)
				break;
			while( (T.size()==0 || !SetOperationsUtility.isSubset(boxT,D)) && T_G.size()>0 )
			{
				Map<AttributeData,Set<Integer>> selectedTfromT_G = selectTfromT_G(T_G,mazSubsetSize,minimumCardinality);
				Set<AttributeData> tempAttributeData = selectedTfromT_G.keySet();
				Iterator<AttributeData> tempAttributeDataIterator = tempAttributeData.iterator();
				AttributeData selectedAttribute = tempAttributeDataIterator.next();
				
				//Add the attribute name to T (eg: T= <(education,primary),(skills,low)>)
				T.add(selectedAttribute);
				// calculate boxT = intersection of values of all selected T's
				for(AttributeData t : T)
				{
					for(AttributeData tempAttribute : attributeValuePairs)
					{
						if(tempAttribute.getAttributeName().equalsIgnoreCase(t.getAttributeName()))
						{
							if(boxT.size()==0)
								boxT.addAll(tempAttribute.getValues());
							else
							{
								Set<Integer> tempBoxT = new LinkedHashSet<Integer>();
								tempBoxT.addAll(boxT);
								boxT.removeAll(tempBoxT);
								boxT.addAll(SetOperationsUtility.intersection(tempBoxT,tempAttribute.getValues()));
							}
							break;
						}
					}
				}
				
				//Update the Value of G (G= G intersection [t])
				Set<Integer> tempG = new LinkedHashSet<Integer>();
				tempG.addAll(G);
				G.removeAll(tempG);
				G.addAll(SetOperationsUtility.intersection(tempG,selectedTfromT_G.get(selectedAttribute)));
				//Add the part of evaluating Ts and Tn and then update tempAttributeValuePairs and evaluate latest T_G
				//For removing Symbolic attributes
				for(AttributeData tempAttributeValuePair : tempAttributeValuePairs)
				{
					if(!tempAttributeValuePair.isNumeric())
					{
						if(selectedAttribute.getDomainName().equalsIgnoreCase(tempAttributeValuePair.getDomainName()))
							TSymbolic.add(tempAttributeValuePair);
					}
					else
					{
						if(selectedAttribute.getDomainName().equalsIgnoreCase(tempAttributeValuePair.getDomainName()))
						{
							Set<Integer> selectedAttributeValues = selectedAttribute.getValues();
							if(SetOperationsUtility.isSubset(selectedAttributeValues, tempAttributeValuePair.getValues()))
							{
								TNumeric.add(tempAttributeValuePair);
							}
							else if(SetOperationsUtility.areDisjointSets(selectedAttribute,tempAttributeValuePair))
							{
								TNumeric.add(tempAttributeValuePair);
							}
						}
					}
				}
				tempAttributeValuePairs.removeAll(TSymbolic);
				tempAttributeValuePairs.removeAll(TNumeric);
				ArrayList<Map<AttributeData,Set<Integer>>> tempT_G = new ArrayList<Map<AttributeData,Set<Integer>>>();
				tempT_G.addAll(T_G);
				T_G.removeAll(tempT_G);
				determineT_G(T_G,tempAttributeValuePairs,G);
			}
			
			//Calculating the probability and Update the fatT and fatJ
			double intersectionSize = (double)SetOperationsUtility.intersection(X, boxT).size();
			double boxTSize = (double)boxT.size();
			double probability = intersectionSize/boxTSize;
			if(probability >= alphaParameter)
			{
				D.addAll(boxT);
				rules.add(T);
				globalBoxT.addAll(boxT);
			}
			else
			{
				junk.add(T);
				globalBoxT.addAll(boxT);
			}
			
			//update the value of G
			G = SetOperationsUtility.subtractSetAFromSetB(globalBoxT,D);
		}

		//Rule Dropping and redundancy check
		//Checking for Rule Dropping conditions
		for(ArrayList<AttributeData> ruleList : rules)  //For each T in fatT
		{
			ArrayList<AttributeData> tempRulesList = new ArrayList<AttributeData>(); 
			tempRulesList.addAll(ruleList); // Place T in tempRulesList
			for(AttributeData rule: tempRulesList) //for each t in T
			{
				ruleList.remove(rule);// remove that t from tempRulesList. T remains as is.
				if(ruleList.size()>0)
				{
					Set<Integer> intersection = new LinkedHashSet<Integer>();
					intersection.addAll(ruleList.get(0).getValues()); //Place the first values in the intersection set
					for(AttributeData tempRule : ruleList) //for all the other rules than current rule find intesection
					{
						Set<Integer> tempIntersection = SetOperationsUtility.intersection(tempRule.getValues(), intersection);
						intersection.removeAll(intersection);
						intersection.addAll(tempIntersection);
						
					}
					if(!SetOperationsUtility.isSubset(intersection, D)) //If the intersection of all other rules except current rule is not subset of D add back the removed rule
						ruleList.add(rule);
				}
				else
					ruleList.add(rule); //final rule to be removed. So don't remove.
			}
		}
		
		//Checking for redundancy
		Set<Integer> casesCoveredByEachRuleSet = new LinkedHashSet<Integer>();
		ArrayList<String> finalRules_local = new ArrayList<String>();
		for(ArrayList<AttributeData> ruleList : rules)
		{
			StringBuffer ruleString = new StringBuffer("");
			Set<Integer> casesCovered = new LinkedHashSet<Integer>();
			casesCovered.addAll(ruleList.get(0).getValues()); //Place the first values in the intersection set
			for(int i=0;i<ruleList.size();i++) //for all the other rules than current rule find intesection
			{
				Set<Integer> intersection = new LinkedHashSet<Integer>();
				intersection = SetOperationsUtility.intersection(ruleList.get(i).getValues(), casesCovered);
				casesCovered.removeAll(casesCovered);
				casesCovered.addAll(intersection);
				ruleString.append(ruleList.get(i).getAttributeName());
				if(i!=ruleList.size()-1)
					ruleString.append(" & ");
			}
			ruleString.append(" -> ").append(concept.getAttributeName());
			if(casesCoveredByEachRuleSet.size()==0)
			{
				casesCoveredByEachRuleSet.addAll(casesCovered);
				finalRules_local.add(ruleString.toString());
			}
			else
			{
				if(!SetOperationsUtility.isSubset(casesCovered, casesCoveredByEachRuleSet))
				{
					finalRules_local.add(ruleString.toString());
					casesCoveredByEachRuleSet.addAll(casesCovered);
				}
			}
		}
		return finalRules_local;
		
	}
	
	private static void determineT_G(ArrayList<Map<AttributeData, Set<Integer>>> T_G, ArrayList<AttributeData> tempAttributeValuePairs, Set<Integer> G)
	{
		mazSubsetSize = 0;
		for(AttributeData attributeValuePair : tempAttributeValuePairs)
		{
			Set<Integer> matchingSubSet = SetOperationsUtility.intersection(attributeValuePair.getValues(),G);
			if(matchingSubSet.size()>0)
			{
				Map<AttributeData,Set<Integer>> map = new LinkedHashMap<AttributeData,Set<Integer>>();
				map.put(attributeValuePair, matchingSubSet);
				T_G.add(map);
				if(matchingSubSet.size()>mazSubsetSize)
					mazSubsetSize = matchingSubSet.size();
			}
		}		
	}

	private static Map<AttributeData, Set<Integer>> selectTfromT_G(ArrayList<Map<AttributeData, Set<Integer>>> T_G,int mazSubsetSize, int minimumCardinality)
	{
		//Procedure for selecting a t from T_G
		Map<AttributeData,Set<Integer>> selectedTfromT_G = new HashMap<AttributeData,Set<Integer>>(); 
		ArrayList<Map<AttributeData,Set<Integer>>> valuesOfTWithMatchingSize = new ArrayList<Map<AttributeData,Set<Integer>>>();
		ArrayList<Map<AttributeData,Set<Integer>>> valuesOfTWithMatchingCardinality = new ArrayList<Map<AttributeData,Set<Integer>>>();
		
		//1. Pick out all the t that have maximum size.
		for(Map<AttributeData,Set<Integer>> map:T_G)
		{
			Set<AttributeData> tempAttributeData = map.keySet();
			Iterator<AttributeData> tempAttributeDataIterator = tempAttributeData.iterator();
			if((map.get(tempAttributeDataIterator.next())).size()==mazSubsetSize)
				valuesOfTWithMatchingSize.add(map);
		}
		
		//If a Tie occurs wrt to maximum set size then check for cardinality
		if(valuesOfTWithMatchingSize.size()>1)
		{
			//calculate the minimum cardinality among the sets that have tie wrt size
			int tempMinimumCardinality = minimumCardinality;
			for(Map<AttributeData,Set<Integer>> map:valuesOfTWithMatchingSize)
			{
				//determine max size among all the available sets
				Set<AttributeData> tempAttributeData = map.keySet();
				Iterator<AttributeData> tempAttributeDataIterator = tempAttributeData.iterator();
				AttributeData tempAttribute = tempAttributeDataIterator.next();
				if(tempAttribute.getCardinality()<tempMinimumCardinality)
					tempMinimumCardinality = tempAttribute.getCardinality();
			}
			for(Map<AttributeData,Set<Integer>> map:valuesOfTWithMatchingSize)
			{
				Set<AttributeData> tempAttributeData = map.keySet();
				Iterator<AttributeData> tempAttributeDataIterator = tempAttributeData.iterator();
				AttributeData tempAttribute = tempAttributeDataIterator.next();
				if(tempAttribute.getCardinality()==tempMinimumCardinality)
					valuesOfTWithMatchingCardinality.add(map);
			}
			
			selectedTfromT_G = valuesOfTWithMatchingCardinality.get(0);
		}
		else
		{
			selectedTfromT_G = valuesOfTWithMatchingSize.get(0);
		}
		return selectedTfromT_G;
	}
}
