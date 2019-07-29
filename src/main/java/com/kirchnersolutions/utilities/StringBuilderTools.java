package com.kirchnersolutions.utilities;

import java.util.ArrayList;
import java.util.List;

public class StringBuilderTools {

    public static StringBuilder replaceExpression(StringBuilder original, String expression, String replacement){
        StringBuilder newString = new StringBuilder(original);
        boolean done = false;
        while(!done){
            int ind = original.indexOf(expression);
            if(ind != -1){
                newString = newString.replace(ind, expression.toCharArray().length - 1 + ind, replacement);
                break;
            }else{
                done = true;
            }
        }
        return newString;
    }

    public static List<StringBuilder> splitStringBuilder(StringBuilder string, char breakChar){
        ArrayList<StringBuilder> strings = new ArrayList<>();
        ArrayList<CharWrapper> chars = new ArrayList<>();
        char[] builderChars = new char[string.length()];
        string.getChars(0, string.length() - 1, builderChars, 0);
        int count = 0;
        for(char ch : builderChars){
            if(ch == breakChar){
                strings.add(buildBuilder(chars));
                chars = new ArrayList<>();
            }else{
                chars.add(new CharWrapper(ch));
            }
        }
        return strings;
    }

    public static byte[] getBytes(StringBuilder string){
        char[] builderChars = new char[string.length()];
        byte[] result = new byte[string.length()];
        string.getChars(0, string.length() - 1, builderChars, 0);
        int count = 0;
        for(char ch : builderChars){
            result[count] = (byte)ch;
            count++;
        }
        return result;
    }

    public static StringBuilder revertBytes(byte[] bytes){
        char[] chars = new char[bytes.length];
        for(int i = 0; i < chars.length; i++){
            chars[i] = (char)bytes[i];
        }
        StringBuilder results = new StringBuilder("");
        results.append(chars);
        return results;
    }

    private static StringBuilder buildBuilder(ArrayList<CharWrapper> chars){
        char[] builderChars = new char[chars.size()];
        int count = 0;
        for(CharWrapper wrapper : chars){
            builderChars[count] = wrapper.getChar();
            count++;
        }
        chars = new ArrayList<>();
        return new StringBuilder("").append(builderChars);
    }


}
