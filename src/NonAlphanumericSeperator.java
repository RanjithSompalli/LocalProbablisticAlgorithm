import java.util.regex.Pattern;


public class NonAlphanumericSeperator {

	public static void main(String[] args) 
	{
		String str1 = "123%sdef$qweerrr:";
		Pattern p= Pattern.compile("^[A-Za-z0-9]");
		String[] lineData = str1.split(p.toString());
		System.out.println("String parsed:::");
		for(String str : lineData)
		{
			System.out.println(str);
		}

	}

}
