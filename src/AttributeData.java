import java.util.Set;


public class AttributeData 
{
	private String attributeType;
	private String attributeName;
	private Set<Integer> values;
	private int cardinality;
	private boolean isNumeric;
	private String domainName;
	private double range1;
	private double range2;
	
	
	public String getAttributeType() {
		return attributeType;
	}
	public void setAttributeType(String attributeType) {
		this.attributeType = attributeType;
	}
	public String getAttributeName() {
		return attributeName;
	}
	public void setAttributeName(String attributeName) {
		this.attributeName = attributeName;
	}
	public Set<Integer> getValues() {
		return values;
	}
	public void setValues(Set<Integer> values) {
		this.values = values;
	}
	public int getCardinality() {
		return cardinality;
	}
	public void setCardinality(int cardinality) {
		this.cardinality = cardinality;
	}
	public boolean isNumeric() {
		return isNumeric;
	}
	public void setNumeric(boolean isNumeric) {
		this.isNumeric = isNumeric;
	}
	public String getDomainName() {
		return domainName;
	}
	public void setDomainName(String domainName) {
		this.domainName = domainName;
	}
	public double getRange1() {
		return range1;
	}
	public void setRange1(double range1) {
		this.range1 = range1;
	}
	public double getRange2() {
		return range2;
	}
	public void setRange2(double range2) {
		this.range2 = range2;
	}
}
