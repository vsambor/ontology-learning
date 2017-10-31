import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.*;

import java.util.ArrayList;

public class Main {

  public static final String ENDPOINT = "http://dblp.l3s.de/d2r/sparql"; //"http://dbpedia.org/sparql";

  public static void main(String[] args) {
    // Initialize needed variables.
    ArrayList<Double> p1Arr = new ArrayList<Double>();
    ArrayList<Double> p2Arr = new ArrayList<Double>();
    Model model = ModelFactory.createDefaultModel();
    int m = 0;
    int s = 0;

    // First query which gets all classes.
    String strQuery1 = "SELECT DISTINCT ?class WHERE { ?_ a ?class }";
    ResultSet result1 = getResult(strQuery1);
    ArrayList<String> classes = getAllClasses(result1);

    // Loops through all classes and perform a count query for each.
    for (int i = 0; i < classes.size(); ++i) {
      String c = classes.get(i);

      String strQuery2 = "SELECT (count(DISTINCT ?x) AS ?m) WHERE { ?x a <" + c + "> . }";
      ResultSet result2 = getResult(strQuery2);

      m = ((Literal) result2.next().get("m")).getInt();

      // Loops through all classes which are different than current one and executes a count query for each.
      for (int j = i + 1; j < classes.size(); ++j) {
        String d = classes.get(j);

        String strQuery3 = "SELECT (count(DISTINCT ?x) AS ?s) WHERE { ?x a <" + c + "> . ?x a <" + d + "> }";
        ResultSet result3 = getResult(strQuery3);

        s = ((Literal) result3.next().get("s")).getInt();

        System.out.println(c + " ---> " + d);

        // First Formula
        double res1 = waldScore(s, m);

        System.out.println(res1);

        p1Arr.add(res1);
        ////////////////////////

        // Second Formula
        double res2 = waldScore(s + 2, m + 4);

        System.out.println(res2);

        p2Arr.add(res2);
        ////////////////////////
      }
    }

    System.out.println("p1Arr = " + p1Arr.toString());
    System.out.println("p2Arr = " + p2Arr.toString());
  }

  /**
   * Calculates the wald Score.
   *
   * @param s - represents the number of distinct couples.
   * @param m - represents the total classes in the db.
   * @return - the Wald's confidence interval.
   */
  public static double waldScore(int s, int m) {
    final double Z = 1.96;

    double p = (double) s / (double) m;
    double waldTop = p + Z * Math.sqrt((p * (1.0 - p)) / (double) m);
    double waldBottom = p - Z * Math.sqrt((p * (1.0 - p)) / (double) m);

    double res = (waldTop + waldBottom) / 2;
    if (res > 1) {
      res = 1;
    } else if (res < 0) {
      res = 0;
    }
    return res;
  }

  /**
   * Execute's a SPARQL query and return a resultSet.
   *
   * @param queryString - the SPARQL query.
   * @return - a resultSet with query results.
   */
  public static ResultSet getResult(String queryString) {
    Query query = QueryFactory.create(queryString);
    return QueryExecutionFactory.sparqlService(ENDPOINT, query).execSelect();
  }

  /**
   * Gets all classes from the resultSet and transform them into arrayList for easy manipulation.
   *
   * @param res - the query result with all classes.
   * @return - an array list with all the classes.
   */
  public static ArrayList<String> getAllClasses(ResultSet res) {
    ArrayList<String> classes = new ArrayList<String>();
    while (res.hasNext()) {
      classes.add(res.next().get("class").toString());
    }
    return classes;
  }
}
