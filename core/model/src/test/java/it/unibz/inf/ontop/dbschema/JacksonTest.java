package it.unibz.inf.ontop.dbschema;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

public class JacksonTest {
    @Test
    public void test(){
        ObjectMapper mapper = new ObjectMapper();

        try {

            // JSON file to Java object
             staff = mapper.readValue(new File("c:\\test\\staff.json"), Staff.class);

            // JSON string to Java object
            String jsonInString = "{\"name\":\"mkyong\",\"age\":37,\"skills\":[\"java\",\"python\"]}";
            Staff staff2 = mapper.readValue(jsonInString, Staff.class);

            // compact print
            System.out.println(staff2);

            // pretty print
            String prettyStaff1 = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(staff2);

            System.out.println(prettyStaff1);


        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
