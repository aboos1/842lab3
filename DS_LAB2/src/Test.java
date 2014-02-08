import java.io.FileNotFoundException;


public class Test 
{
	public static void main(String[] args)
	{
		MessagePasser mp = new MessagePasser("adbc", "123");
		
		try 
		{
			mp.parseConfig();
		} 
		catch (FileNotFoundException e) 
		{
			e.printStackTrace();
		}
	}
}
