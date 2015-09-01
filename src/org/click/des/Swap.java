package org.click.des;

public class Swap {

	
	
	
	public static void main(String[] args)
	{
		Swap sw=new Swap();
		
		String str="因此在每次对 String 类型进行改变的时候其实都等同于生成了一个新的 String 对象，然后将指针指向新的 String 对象,String S1 = “This is only a simple test”; ";
		String str2=sw.swap(str);
		System.err.println("str:"+str);
		System.err.println("str2:"+str2);
		
		String str3=sw.reswap(str2);
		
		System.err.println("str3:"+str3);
		
	}
	
	
	
	
	
	
	
	
	
	public String swap(String str)
	{
		String res="";
		StringBuilder sb=new StringBuilder(str);
		
		int temp=0;
		
		for(int i=0;i<str.length();i++)
		{
			if(i%7==0)
			{
				temp=sb.charAt(i);
				sb.setCharAt(i,(char) (temp+5));
			}
		}
		
		res=sb.toString();
		
		return res;
	}
	
	public String reswap(String str)
	{
		String res="";
		StringBuilder sb=new StringBuilder(str);
		
		int temp=0;
		
		for(int i=0;i<str.length();i++)
		{
			if(i%7==0)
			{
				temp=sb.charAt(i);
				sb.setCharAt(i,(char) (temp-5));
			}
		}
		
		res=sb.toString();
		
		return res;
	}
	
}
