package com.droi.account.statis;

import java.util.HashMap;
import java.util.Map;

import android.text.TextUtils;

import com.droi.account.DebugUtils;


public class DroiEncoder {

	private static Map<Character, Character> mCharMap = new HashMap<Character, Character>();
	static{
		mCharMap.put('a', 'y');
		mCharMap.put('/', 'T');
		mCharMap.put('c', '/');
		mCharMap.put('b', 'z');
		mCharMap.put('e', 'r');
		mCharMap.put('d', 'J');
		mCharMap.put('g', '7');
		mCharMap.put('f', '5');
		mCharMap.put('i', '8');
		mCharMap.put('h', 'g');
		mCharMap.put('k', 'M');
		mCharMap.put('j', 'd');
		mCharMap.put('m', 'R');
		mCharMap.put('l', 't');
		mCharMap.put('o', 'a');
		mCharMap.put('n', 'V');
		mCharMap.put('q', 'v');
		mCharMap.put('p', 'G');
		mCharMap.put('s', '0');
		mCharMap.put('r', 'X');
		mCharMap.put('u', 'm');
		mCharMap.put('t', 'l');
		mCharMap.put('7', 'i');
		mCharMap.put('6', 'O');
		mCharMap.put('9', 'U');
		mCharMap.put('8', 'W');
		mCharMap.put('+', 'D');
		mCharMap.put('z', 'k');
		mCharMap.put('0', 'F');
		mCharMap.put('w', 'B');
		mCharMap.put('v', '6');
		mCharMap.put('y', 'j');
		mCharMap.put('A', 'x');
		mCharMap.put('x', 'w');
		mCharMap.put('C', 'Q');
		mCharMap.put('B', 'e');
		mCharMap.put('E', '3');
		mCharMap.put('D', '2');
		mCharMap.put('G', 'q');
		mCharMap.put('F', '1');
		mCharMap.put('I', 'Z');
		mCharMap.put('H', 'S');
		mCharMap.put('K', 'E');
		mCharMap.put('J', 'n');
		mCharMap.put('M', 'H');
		mCharMap.put('L', 'A');
		mCharMap.put('O', 'o');
		mCharMap.put('N', 'p');
		mCharMap.put('Q', '+');
		mCharMap.put('P', '4');
		mCharMap.put('S', 'C');
		mCharMap.put('R', 'P');
		mCharMap.put('U', 'c');
		mCharMap.put('T', 's');
		mCharMap.put('W', 'b');
		mCharMap.put('V', 'u');
		mCharMap.put('Y', 'h');
		mCharMap.put('X', 'N');
		mCharMap.put('1', 'L');
		mCharMap.put('Z', 'K');
		mCharMap.put('3', 'f');
		mCharMap.put('2', '9');
		mCharMap.put('5', 'Y');
		mCharMap.put('4', 'I');
		mCharMap.put(' ', 'D');
	}
	
	public DroiEncoder() {
		// TODO Auto-generated constructor stub
	}
	
	public static String encode(String info){

		if(!TextUtils.isEmpty(info)){
			if(DebugUtils.DEBUG){
				DebugUtils.i("replace", "map size = " + mCharMap.size());
			}
			StringBuilder stringBuilder = new StringBuilder(info);
			int length = stringBuilder.length();
			for(int i = 0; i < length; i++){
				char c = stringBuilder.charAt(i);
				Character mapValue = mCharMap.get(c);
				if(mapValue != null){
					stringBuilder.replace(i, i+1, mapValue.toString());
				}
			}
			return stringBuilder.toString();
			
			/*
			 * 			
			Iterator<Entry<Character, Character>> it = mCharMap.entrySet().iterator();
			while(it.hasNext()){
				if(DebugUtils.DEBUG){
					DebugUtils.i("replace", "hasNext ");
				}
				Map.Entry<Character, Character> entry = (Map.Entry<Character, Character>)it.next();
				char key = entry.getKey();
				char value = entry.getValue();
				if(DebugUtils.DEBUG){
					DebugUtils.i("replace", key + " to " + value);
				}
				info.replace(key, value);
				if(DebugUtils.DEBUG){
					DebugUtils.i("replace", "info = " + info);
				}
			}*/
		}
		
		return "";
	}

}