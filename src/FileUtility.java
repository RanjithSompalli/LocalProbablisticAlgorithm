import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Pattern;


public class FileUtility 
{
	int attributeCount = 0;
	ArrayList<String> attributeAndConceptNames = null;
	ArrayList<AttributeData> attributeValuePairs = new ArrayList<AttributeData>();
	
	public ArrayList<AttributeData> readInputFileData(String inputFileName) throws IOException
	{
		FileInputStream fstream = null;
		BufferedReader reader = null;
		
		try 
		{
			fstream = new FileInputStream(inputFileName);
			reader = new BufferedReader(new InputStreamReader(fstream));
			String stringLine;
			
			if(reader != null)
			{
				Map<String,ArrayList<String>> tempAttributeValuePairs = new LinkedHashMap<String,ArrayList<String>>();
				//Read File Line By Line
				while ((stringLine = reader.readLine()) != null)  
				{
					Pattern p= Pattern.compile("(\\s*(!.*((\\r\\n)|(\\n)|(\\r)))\\s*)|(\\s+)");
					String[] lineData = stringLine.split(p.toString());
					if(lineData.length > 0)
					{
						if(lineData[0].equalsIgnoreCase("<"))
						{
							for(int i=0; i<lineData.length; i++)
							{
								if(lineData[i].equals("a"))
									attributeCount++;						
							}
						}
						else if(lineData[0].equalsIgnoreCase("["))
						{
							attributeAndConceptNames = new ArrayList<String>();
							for(int i=1; i<lineData.length-1; i++)
							{	
								attributeAndConceptNames.add(lineData[i]);
							}
							for(int j=0; j<attributeAndConceptNames.size(); j++)
							{
								tempAttributeValuePairs.put(attributeAndConceptNames.get(j),new ArrayList<String>());
							}
							
						}
						else if(lineData[0].equalsIgnoreCase("!"))
						{
							continue;
						}
						else
						{
							for(int i=0;i<attributeAndConceptNames.size();i++)
							{
								String attributeName = attributeAndConceptNames.get(i);
								ArrayList<String> tempAttributeValue = tempAttributeValuePairs.get(attributeName);
								tempAttributeValuePairs.remove(attributeName);
								tempAttributeValue.add(lineData[i]);
								tempAttributeValuePairs.put(attributeName, tempAttributeValue);	
							}
						}
					}
				}
				
				attributeValuePairs = formatAttributeValuePairs(tempAttributeValuePairs);
			}
		}
		catch (FileNotFoundException e) 
		{
			e.printStackTrace();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		finally
		{
			reader.close();
		}
		return attributeValuePairs;
	}

	private ArrayList<AttributeData> formatAttributeValuePairs(Map<String, ArrayList<String>> attributeValuePairs)
	{
		ArrayList<AttributeData> tempActualAttributeValuePairs = new ArrayList<AttributeData>();

		Set<String> mapValues = attributeValuePairs.keySet();
		Iterator<String> iterator = mapValues.iterator();
		String conceptName= attributeAndConceptNames.get(attributeAndConceptNames.size()-1);
		ArrayList<String> conceptValues = attributeValuePairs.get(conceptName);
		
		int tempAttributeCount = 1;
		//looping for each attribute
		while(iterator.hasNext())
		{

			boolean isNumericAttribute = false;
			Map<String,ArrayList<Integer>> attributeValueMapWithoutDupicates = new LinkedHashMap<String,ArrayList<Integer>>();

			String attributeName = iterator.next();
			ArrayList<String> tempAttributeValues = attributeValuePairs.get(attributeName);

			//Populate a map with the attribute value (without duplication) as key and a value as Empty Array List 
			//eg: <38,empty array list>, <yes, empty arraylist>
			for(int i=0; i<tempAttributeValues.size();i++)
			{
				String tempAttributeValue = tempAttributeValues.get(i);
				if(!tempAttributeValue.equalsIgnoreCase("*") && !tempAttributeValue.equalsIgnoreCase("?") && !tempAttributeValue.equalsIgnoreCase("-"))
				{
					if(!attributeValueMapWithoutDupicates.containsKey(tempAttributeValue))
						attributeValueMapWithoutDupicates.put(tempAttributeValue,new ArrayList<Integer>());
				}
				
			}

			isNumericAttribute = SetOperationsUtility.isNumeric(attributeValueMapWithoutDupicates);
			
			if(tempAttributeCount > attributeCount)
				isNumericAttribute = false;
			//For Handling Numeric Attributes
			if(isNumericAttribute)
			{
				ArrayList<Double> numericAttributes = new ArrayList<Double>();
				Set<String> attributeValueMapKeys_local = attributeValueMapWithoutDupicates.keySet();
				Iterator<String> attributeValueIterator_local = attributeValueMapKeys_local.iterator();
				String[] attributesToBeRemoved = new String[attributeValueMapWithoutDupicates.size()];
				int pos =0;
				while(attributeValueIterator_local.hasNext())
				{
					String attributeValue_local = attributeValueIterator_local.next();
					attributesToBeRemoved[pos]=attributeValue_local;
					numericAttributes.add(Double.parseDouble(attributeValue_local));
					pos++;
				}
				for(int i=0;i<attributesToBeRemoved.length;i++)
				{
					attributeValueMapWithoutDupicates.remove(attributesToBeRemoved[i]);
				}
				
				Collections.sort(numericAttributes);
				for(int i=0;i<numericAttributes.size()-1;i++)
				{
					double midPoint = (numericAttributes.get(i)+numericAttributes.get(i+1))/2;
					String midPointRange1 = "("+" "+attributeName+" "+","+" "+numericAttributes.get(0)+".."+midPoint+" "+")";
					String midPointRange2 = "("+" "+attributeName+" "+","+" "+midPoint+".."+numericAttributes.get(numericAttributes.size()-1)+" "+")";
					ArrayList<Integer> range1 = new ArrayList<Integer>();
					ArrayList<Integer> range2 = new ArrayList<Integer>();
					
					int k=1;
					for(int j=0; j<tempAttributeValues.size();j++)
					{
						String tempAttributeValueString = tempAttributeValues.get(j);
						if(!tempAttributeValueString.equalsIgnoreCase("*") && !tempAttributeValueString.equalsIgnoreCase("?") && !tempAttributeValueString.equalsIgnoreCase("-"))
						{
							double tempAttributeValue = Double.parseDouble(tempAttributeValueString);
							if(tempAttributeValue>=numericAttributes.get(0) && tempAttributeValue<= midPoint)
							{
								range1.add(k);
							}
							else if(tempAttributeValue>=midPoint && tempAttributeValue<= numericAttributes.get(numericAttributes.size()-1))
							{
								range2.add(k);
							}
						}
						else if(tempAttributeValueString.equalsIgnoreCase("*"))
						{
							range1.add(k);
							range2.add(k);
						}
						else if(tempAttributeValueString.equalsIgnoreCase("-"))
						{
							ArrayList<Integer> matchingPositions = new ArrayList<Integer>();
							ArrayList<String> matchingAttributeValues = new ArrayList<String>();
							String matchingConceptValue = conceptValues.get(j);
							for(int l=0;l<conceptValues.size();l++)
							{
								if(conceptValues.get(l).equalsIgnoreCase(matchingConceptValue))
								{
									matchingPositions.add(l);
								}
							}
							for(Integer matchingPosition: matchingPositions)
							{
								matchingAttributeValues.add(tempAttributeValues.get(matchingPosition));
							}
							for(String matchingAttributeValue:matchingAttributeValues)
							{
								if(!matchingAttributeValue.equalsIgnoreCase("*") && !matchingAttributeValue.equalsIgnoreCase("?") && !matchingAttributeValue.equalsIgnoreCase("-"))
								{
									double tempAttributeValue = Double.parseDouble(matchingAttributeValue);
									if(tempAttributeValue>=numericAttributes.get(0) && tempAttributeValue<= midPoint)
									{
										range1.add(k);
									}
									else if(tempAttributeValue>=midPoint && tempAttributeValue<= numericAttributes.get(numericAttributes.size()-1))
									{
										range2.add(k);
									}
								}
							}
						}
						k++;
					}

					attributeValueMapWithoutDupicates.put(midPointRange1, range1);
					attributeValueMapWithoutDupicates.put(midPointRange2, range2);
				}
			}
			//For Handling Symbolic Attributes
			else
			{
				Set<String> attributeValueMapKeys_local = attributeValueMapWithoutDupicates.keySet();
				Iterator<String> attributeValueIterator_local = attributeValueMapKeys_local.iterator();
				while(attributeValueIterator_local.hasNext())
				{
					String attributeValue_local = attributeValueIterator_local.next();
					int j=1;
					for(int i=0; i<tempAttributeValues.size();i++)
					{
						String tempAttributeValue = tempAttributeValues.get(i);

						if(tempAttributeValue.equals(attributeValue_local))
							attributeValueMapWithoutDupicates.get(attributeValue_local).add(j);
						else if(tempAttributeValue.equalsIgnoreCase("*"))
							attributeValueMapWithoutDupicates.get(attributeValue_local).add(j);
						else if(tempAttributeValue.equalsIgnoreCase("-"))
						{
							ArrayList<Integer> matchingPositions = new ArrayList<Integer>();
							ArrayList<String> matchingAttributeValues = new ArrayList<String>();
							String matchingConceptValue = conceptValues.get(i);
							for(int k=0;k<conceptValues.size();k++)
							{
								if(conceptValues.get(k).equalsIgnoreCase(matchingConceptValue))
								{
									matchingPositions.add(k);
								}
							}
							for(Integer pos: matchingPositions)
							{
								matchingAttributeValues.add(tempAttributeValues.get(pos));
							}
							for(String matchingAttributeValue:matchingAttributeValues)
							{
								if(matchingAttributeValue.equals(attributeValue_local))
								{
									if(!attributeValueMapWithoutDupicates.get(attributeValue_local).contains(j))
										attributeValueMapWithoutDupicates.get(attributeValue_local).add(j);
								}
							}
						}
							
						j++;
					}
				}
			}

			//For constructing the attribute value pairs
			Set<String> tempAttributeValueMapKeys = attributeValueMapWithoutDupicates.keySet();
			Iterator<String> tempAttributeValueIterator = tempAttributeValueMapKeys.iterator();
			while(tempAttributeValueIterator.hasNext())
			{
				AttributeData attributeData = new AttributeData();
				//set type of the attribute: attribute or concept
				if(tempAttributeCount > attributeCount)
					attributeData.setAttributeType("concept");
				else
					attributeData.setAttributeType("attribute");
				String tempAttributeValue = tempAttributeValueIterator.next();
				if(isNumericAttribute)
				{
					attributeData.setAttributeName(tempAttributeValue);
					String[] attributeNameArray = tempAttributeValue.split(" ");
					StringTokenizer st = new StringTokenizer(attributeNameArray[3],"..");
					double range1 = Double.parseDouble(st.nextToken());
					double range2 = Double.parseDouble(st.nextToken());
					double range3 = Double.parseDouble(st.nextToken());
					attributeData.setRange1(range1);
					attributeData.setRange2(range3);
				}
				else
				{
					String attributeNameString;
					attributeNameString="("+" "+attributeName+" "+","+" "+tempAttributeValue+" "+")";
					attributeData.setAttributeName(attributeNameString);
				}
				attributeData.setValues(new LinkedHashSet<Integer>(attributeValueMapWithoutDupicates.get(tempAttributeValue)));
				attributeData.setCardinality(attributeValueMapWithoutDupicates.get(tempAttributeValue).size());
				attributeData.setNumeric(isNumericAttribute);
				attributeData.setDomainName(attributeName);
				tempActualAttributeValuePairs.add(attributeData);
			}
			
			tempAttributeCount++;
		}

		return tempActualAttributeValuePairs;
	}
	
	public void writeRulesToFile(String fileName, ArrayList<String> rules)
	{
		try
		{
			File file = new File(fileName);
			file.createNewFile();
			
			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			for(String rule: rules)
			{
				bw.write(rule);
				bw.newLine();
			}
			bw.close(); 
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}
	
}