package com.kirchnersolutions.utilities;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.codehaus.jackson.JsonGenerationException;

/**
 * Java Program to parse JSON String to Java object and converting a Java object to equivalent
 * JSON String.
 *
 * @author Javin Paul
 * https://mvnrepository.com/artifact/org.codehaus.jackson/jackson-core-asl/1.9.13
 *
 * Modified By Robert Kirchner Jr.
 */
public class JSONParser {

    public static void main(String args[]) {
        toJSON();  // converting Java object to JSON String
        toJava();  // parsing JSON file to create Java object
    }

    /**
     * Method to parse JSON String into Java Object using Jackson Parser.
     *
     */
    /*
    public static void toJava() {

        // this is the key object to convert JSON to Java
        ObjectMapper mapper = new ObjectMapper();

        try {
            File json = new File("player.json");
            Player cricketer = mapper.readValue(json, Player.class);
            System.out.println("Java object created from JSON String :");
            System.out.println(cricketer);

        } catch (JsonGenerationException ex) {
            ex.printStackTrace();
        } catch (JsonMappingException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();

        }
    }

     */

    /**
     * Java method to convert Java Object into JSON String with help of Jackson API.
     *
     */
    public static void tableStatToJSON() {

        // our bridge from Java to JSON and vice versa
        ObjectMapper mapper = new ObjectMapper();

        try {
            File json = new File("player.json");
            mapper.writeValue(json, kevin);
            System.out.println("Java object converted to JSON String, written to file");
            System.out.println(mapper.writeValueAsString(kevin));

        } catch (JsonGenerationException ex) {
            ex.printStackTrace();
        } catch (JsonMappingException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();

        }
    }

}