package KeywordSearchEngine.model;

import KeywordSearchEngine.util.MessageHandler;
import KeywordSearchEngine.util.TFIDFCalculator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.*;
import java.util.StringTokenizer;
import org.bson.Document;

/**
 * @author Zhongjie Shen
 */
public class InvertedIndexBuilder {

  Map<String, ArrayList<String>> dict_fullName_skills; // dict[fullName] = [skill]
  Map<String, ArrayList<String>> dict_skill_fullNames; // dict[skill] = [fullName]
  Map<String, Double> tfidfList;

  TFIDFCalculator calculator;
 
  public InvertedIndexBuilder() {
    // init all dicts
    dict_fullName_skills = new HashMap<>();
    dict_skill_fullNames = new HashMap<>();
    tfidfList = new HashMap<>();

    this.calculator = new TFIDFCalculator();

    return;
  }

  /**
   * add tokens into a pre-calculated list
   *
   * @param fullName full name of the target
   * @param document document
   */
  public void add_token(String fullName, String document) {
    StringTokenizer st = new StringTokenizer(document, ",");

    ArrayList<String> skills = new ArrayList<>();

    while (st.hasMoreTokens()) {
      String token = st.nextToken().trim();

      // for dict_fullName_skills
      skills.add(token);

      // for dict_skill_fullNames
      ArrayList<String> fullNames = this.dict_skill_fullNames.get(token);
      if (fullNames == null) {
        fullNames = new ArrayList<>();
      }

      fullNames.add(fullName);
      this.dict_skill_fullNames.put(token, fullNames);
    }
    // for dict_fullName_skills
    dict_fullName_skills.put(fullName, skills);
    return;
  }

  /**
   * add tokens into a pre-calculated list
   *
   * @param fullName full name of the target
   * @param document document
   * @param handler  DBHandler
   */
  public void add_token(String fullName, String document, DBHandler handler) {
    StringTokenizer st = new StringTokenizer(document, ",");

    ArrayList<String> skills = new ArrayList<>();

    while (st.hasMoreTokens()) {
      String token = st.nextToken().trim();

      // for dict_fullName_skills
      skills.add(token);

      // for dict_skill_fullNames
      ArrayList<String> fullNames = this.dict_skill_fullNames.get(token);
      if (fullNames == null) {
        fullNames = new ArrayList<>();
      }

      fullNames.add(fullName);
      this.dict_skill_fullNames.put(token, fullNames);

      Document doc_skill_fullNames = handler.newDocument(token).append("skills", fullNames);
      handler.updateDocument(doc_skill_fullNames, "skill_fullnames");
    }

    // for dict_fullName_skills - no database
    dict_fullName_skills.put(fullName, skills);

    // for doc_fullName_skills - with database
    Document doc_fullName_skills = handler.newDocument(fullName).append("skills", skills);
    handler.updateDocument(doc_fullName_skills, "fullname_skills");

    return;
  }

  /**
   * calculate tdidf value of all included in raw list
   *
   * @return calculated tdidf list
   */
  public Map<String, Double> calculate() {
    MessageHandler.infoMessage("Start calculating...");

    int doc_total; // total skill count under one entry
    int docs_total = this.calculateDocsTotal(); // total token count under all entries
    int term_occur_in_docs; // total token appearence under all entries

    for (Entry<String, ArrayList<String>> entry : this.dict_skill_fullNames.entrySet()) {
      String skill = entry.getKey();
      ArrayList<String> names = entry.getValue();

      term_occur_in_docs = names.size();

      double tfidf = 0;
      double totalTermCount = names.size();

      for (String name : names) {
        ArrayList<String> skills = this.dict_fullName_skills.get(name);
        doc_total = skills.size();
        double temp = calculator.tfIdf(doc_total, docs_total, term_occur_in_docs);
        tfidf = tfidf + temp;
      }

      tfidf = tfidf / totalTermCount;

      this.tfidfList.put(skill, tfidf);
    }

    return this.tfidfList;
  }

  /**
   * calculate tdidf value of all included in raw list and input result to mongodb
   * <p>
   * TODO: need some more improvement. the goal is doing everything with db only. no built-in db
   * structures
   *
   * @return [description]
   */
  public Map<String, Double> calculate(DBHandler handler) {
    MessageHandler.infoMessage("Start calculating...");

    int doc_total; // total skill count under one entry
    int docs_total = this.calculateDocsTotal(); // total token count under all entries
    int term_occur_in_docs; // total token appearence under all entries

    for (Entry<String, ArrayList<String>> entry : this.dict_skill_fullNames.entrySet()) {
      String skill = entry.getKey();
      ArrayList<String> names = entry.getValue();

      term_occur_in_docs = names.size();

      double tfidf = 0;
      double totalTermCount = names.size();

      for (String name : names) {
        ArrayList<String> skills = this.dict_fullName_skills.get(name);
        doc_total = skills.size();
        double temp = calculator.tfIdf(doc_total, docs_total, term_occur_in_docs);
        tfidf = tfidf + temp;
      }

      tfidf = tfidf / totalTermCount;

      // for db insertion
      Document doc_skill_tdidf = handler.newDocument(skill)
          .append("tdidf", tfidf);
      handler.updateDocument(doc_skill_tdidf, "skill_tdidf");

      // for in-built
      this.tfidfList.put(skill, tfidf);
    }

    MessageHandler.infoMessage("db insertion finished: skill_tdidf");

    return this.tfidfList;
  }

  /**
   * print tfidf list
   */
  public void print_tfidfList() {
    for (Map.Entry<String, Double> p : this.tfidfList.entrySet()) {
      MessageHandler.debugMessage("term: " + p.getKey() + " tfidf: " + p.getValue());
    }
    MessageHandler.debugMessage("print_tfidfList");
  }

  /**
   * print fullname and skill list
   */
  public void print_fullName_skill() {
    for (Entry<String, ArrayList<String>> entry : this.dict_fullName_skills.entrySet()) {
      MessageHandler.debugMessage("fullName: " + entry.getKey());
      MessageHandler.debugMessage("skills: " + entry.getValue());
    }
    MessageHandler.debugMessage("print_fullName_skill");
  }

  /**
   * print skill and full name list
   */
  public void print_skill_fullName() {
    for (Entry<String, ArrayList<String>> entry : this.dict_skill_fullNames.entrySet()) {
      MessageHandler.debugMessage("skill: " + entry.getKey());
      MessageHandler.debugMessage("names: " + entry.getValue());
    }
    MessageHandler.debugMessage("print_skill_fullName");
  }

  /**
   * calculate total doc count
   *
   * @return doc total count
   */
  private int calculateDocsTotal() {
    int docs_total = 0;

    for (Entry<String, ArrayList<String>> entry : this.dict_fullName_skills.entrySet()) {
      for (String skill : entry.getValue()) {
        docs_total++;
      }
    }

    return docs_total;
  }
}
