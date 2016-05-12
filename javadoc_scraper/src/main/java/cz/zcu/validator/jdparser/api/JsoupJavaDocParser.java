package cz.zcu.validator.jdparser.api;

import edu.illinois.cs.cogcomp.core.utilities.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;

import com.google.gson.Gson;

import cz.zcu.validator.jdparser.api.javadocs.JavaDocAttribute;
import cz.zcu.validator.jdparser.api.javadocs.JavaDocClass;
import cz.zcu.validator.jdparser.api.javadocs.JavaDocMethod;
import cz.zcu.validator.jdparser.api.javadocs.factory.JavaDocsFactory;
import cz.zcu.validator.jdparser.api.skeletons.SkeletonAttribute;
import cz.zcu.validator.jdparser.api.skeletons.SkeletonClass;
import cz.zcu.validator.jdparser.api.skeletons.SkeletonConstructor;
import cz.zcu.validator.jdparser.api.skeletons.SkeletonMethod;
import cz.zcu.validator.jdparser.api.skeletons.factory.SkeletonFactory;
import cz.zcu.validator.jdparser.data.JavaDocFilesFinder;
import cz.zcu.validator.jdparser.data.JavaDocParsing;
import cz.zcu.validator.jdparser.data.JavaDocParsingException;

/**
 * Parses data from JavaDoc folder into {@code ClassRepresentation} list. An
 * implementation of JavaDocParsing.
 * 
 * @author Paul Vijayakumar
 * 
 * adapted from code by Stepan Cais, scais@kiv.zcu.cz, 3.12.2012
 */
public class JsoupJavaDocParser implements JavaDocParsing {
	
	private class FexClass{	
		
		public FexClass(String name, String description, String testcode, String javadoc_url, String annotations){
		this.name = name;
		this.description = description;
		this.testcode = testcode;
		this.javadoc_url = javadoc_url;
		this.annotations = annotations;
		}
	
		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getDescription() {
			return description;
		}

		public void setDescription(String description) {
			this.description = description;
		}

		public String getTestcode() {
			return testcode;
		}

		public void setTestcode(String testcode) {
			this.testcode = testcode;
		}
		
		public String getJavadoc_url() {
			return javadoc_url;
		}

		public void setJavadoc_url(String javadoc_url) {
			this.javadoc_url = javadoc_url;
		}

		String name, description, testcode = null, javadoc_url = "", annotations = "";
		
		
	}

	/**
	 * Factory for creating skeleton elements.
	 */
	private SkeletonFactory skeletonFactory = null;

	/**
	 * Factory for creating javaDoc elements.
	 */
	private JavaDocsFactory javaDocFactory = null;

	private String base_url;
	
	String[] fexes = {"LabelOneAfter", "LabelOneBefore", "LabelTwoAfter", "POSWindow"};
	

	public JsoupJavaDocParser(String base_url) {

		this.skeletonFactory = new SkeletonFactory();
		this.javaDocFactory = new JavaDocsFactory();
		this.base_url = base_url;
	}
	
	private List<String> buildFexList(){
		
		return Arrays.asList(this.fexes);  
	}

	@Override
	public Collection<ClassRepresentation> parseDataFromJavaDoc(
			File javaDocFolder) throws JavaDocParsingException {

		
		if (!javaDocFolder.isDirectory()) {

			throw new JavaDocParsingException(
					"Given JavaDoc folder is not a folder: "
							+ javaDocFolder.getName()
							+ " Cannot continue with parsing.");
		}

		Collection<File> javaDocClassFiles = JavaDocFilesFinder
				.getJavaDocClassFiles(javaDocFolder);

		Collection<ClassRepresentation> classes = new ArrayList<ClassRepresentation>();

		ArrayList<FexClass> listforjson = new ArrayList<FexClass>();
		
		for (File javaDocClassFile : javaDocClassFiles) {
			
			boolean set = false;
			for(String fex: this.fexes){
				if(javaDocClassFile.getName().contains(fex)){
					System.out.println("We have found the feature extractor.");
					set = true;
				}
			}
			
			if(!set){
				continue;
			}

			Document jsoupDocument = null;

			try {
				// TODO: Maybe usage of
				// http://code.google.com/p/juniversalchardet/
				jsoupDocument = Jsoup.parse(javaDocClassFile, "UTF-8");

			} catch (IOException e) {

				throw new JavaDocParsingException(e);
			}

			try {
				
				SkeletonClass skeletonClass = extractSkeletonClass(jsoupDocument);
				
				JavaDocClass javaDocClass = extractJavaDocClass(jsoupDocument);
				if(set){
					System.out.println(javaDocClass.toString());
				}
				
				Map<SkeletonConstructor, JavaDocMethod> classConstructors = extractConstructors(jsoupDocument);

				Map<SkeletonAttribute, JavaDocAttribute> classAttributes = extractAttributes(jsoupDocument);

				Map<SkeletonMethod, JavaDocMethod> skeletonMethods = extractMethods(jsoupDocument);

				List<String> fexlist = buildFexList();
				
				for(String className : fexlist){
					if(skeletonClass.getName().equals(className)){
						classes.add(new ClassRepresentation(classConstructors,
								classAttributes, skeletonMethods, javaDocClass,
								skeletonClass));
					}
				}
			} catch (Exception e) {

				throw new JavaDocParsingException(
						"Error during checking javadoc in file: "
								+ javaDocClassFile + "\n" + e);
			}
		}
		
		Object [] reps = (Object[]) classes.toArray();
		System.out.println(classes.size());
		for(int i = 0; i < reps.length; i++){
			
			ClassRepresentation a = (ClassRepresentation) reps[i];
			String java_code = "";
			String base_raw_url = "https://raw.githubusercontent.com/pauljv92/testedison/master/edison/src/test/java/edu/illinois/cs/cogcomp/edison/features/lrec/";
			
			JavaDocClass jdoc = a.getJavaDocClass();
			
			try {
					
				   URL url = new URL(base_raw_url+"Test"+jdoc.getName()+".java");
				   Scanner s = new Scanner(url.openStream());
				   while(s.hasNext()) {
					    // operate
					   	java_code += s.nextLine()+"\n";
					}
				   // read from your scanner
				}
				catch(IOException ex) {
				   // there was some connection problem, or the file did not exist on the server,
				   // or your URL was not in the right format.
				   // think about what to do now, and put it here.
				   ex.printStackTrace(); // for now, simply output it.
				}
			
			//.substring(java_code.indexOf("test("))
			//
			//".replaceAll(";", ";\n")";
			listforjson.add(new FexClass(jdoc.getName(),jdoc.getCommentText()+"\nKeywords: "+jdoc.getKeywords().toString(),java_code.substring(java_code.indexOf("test(")),this.base_url+jdoc.getName()+".html",jdoc.getKeywords()));
		}
		
		ClassLoader classLoader = edu.illinois.cs.cogcomp.edison.features.Feature.class.getClassLoader();
		
        //InputStream in = edu.illinois.cs.cogcomp.edison.features.Feature.class.getResourceAsStream(package_structure);
        //System.out.println(in.toString());
        //BufferedReader rdr = new BufferedReader(new InputStreamReader(in));
        
		System.out.println(new Gson().toJson(listforjson));

		return classes;
	}

	/**
	 * Extract JavaDoc class from given document representation in jsoup format.
	 * 
	 * @param jsoupDocument
	 *            Document containing loaded JavaDoc.
	 * @return 
	 * @return New instance of JavaDocClass.
	 */
	private JavaDocClass extractJavaDocClass (Document jsoupDocument) {

		Element elementsSiblingsHR = jsoupDocument.getElementsByTag("hr")
				.first().parent();

		Elements blockElements = elementsSiblingsHR.getElementsByClass("block");

		String name = elementsSiblingsHR.getElementsByTag("span").first()
				.text().trim();

		String deprecated = "";
		String text = "";

		for (Element el : blockElements) {

			if (el.text().contains("Deprecated")) {

				deprecated = el.text().trim();
			} else {

				text = el.text().trim();
			}
		}

		Elements dlElements = elementsSiblingsHR.getElementsByTag("dl");

		String since = "";
		String version = "";
		String author = "";
		String see = "";
		String keywords = "";

		if (dlElements.size() != 0) {

			Elements s = dlElements.last().getElementsByClass("simpleTagLabel");
			for (Element el : s) {
				System.out.println(el.text());
				if (el.text().contains("Since")) {

					since = el.parent().nextElementSibling().text().trim();
				} else if (el.text().contains("Version")) {

					version = el.parent().nextElementSibling().text().trim();
				} else if (el.text().contains("Author")) {

					author = el.parent().nextElementSibling().text().trim();
				} else if (el.text().contains("See")) {

					see = el.parent().nextElementSibling().text().trim();
				} else if (el.text().contains("Keywords")){
					keywords = el.parent().nextElementSibling().text().trim();
				}
			}
		}else{
			return null;
		}

		return javaDocFactory.makeJavaDocClass(name, text, deprecated, since,
				see, author, version, keywords);
	}

	/**
	 * Extract JavaDoc class from given document representation in jsoup format.
	 * 
	 * @param jsoupDocument
	 *            Document containing loaded JavaDoc.
	 * @return New instance of {@code SkeletonClass}.
	 */
	private SkeletonClass extractSkeletonClass(Document jsoupDocument) {

		Elements descriptionElements = jsoupDocument
				.getElementsByClass("description");
		
		Elements preElements = descriptionElements.get(0).getElementsByTag(
				"pre");

		Node acessModifiers = preElements.first().childNodes().get(0);

		Node abc = preElements.first().childNodes().get(1);	
		Node className = null;
		String classNameString = null;
		if(abc.childNodes().size() == 0){
			//System.out.println("abc is NULL");
			
		}
		else{
			//System.out.println("abc is good");
		className = abc.childNode(0);	
		
		}
		//System.out.println("SkeletonClass - getting here 5.");
		
		if(className == null){
			
			classNameString = "null";
			
		}else{
			
			classNameString = className.toString();
			
		}
		
		return skeletonFactory.makeSkeletonClass(classNameString,
				acessModifiers.toString());
	}

	/**
	 * Extract constructor from given document representation in jsoup format.
	 * Constructor is a pair of {@code SkeletonConstrucotr} and
	 * {@code JavadocMethod}.
	 * 
	 * @param jsoupDocument
	 *            Document containing loaded JavaDoc.
	 * @return All constructors saved as pairs in hashmap.
	 */
	private Map<SkeletonConstructor, JavaDocMethod> extractConstructors(
			Document jsoupDocument) {

		Elements counstructorDetailElements = jsoupDocument
				.getElementsByAttributeValue("name", "constructor_detail");

		Map<SkeletonConstructor, JavaDocMethod> constructors = new HashMap<SkeletonConstructor, JavaDocMethod>();

		if (!counstructorDetailElements.isEmpty()) {

			Elements preElements = counstructorDetailElements.parents().first()
					.getElementsByTag("pre");

			for (Element preElement : preElements) {

				JavaDocMethod javaDocConstructor = extractJavaDocConstructor(preElement
						.parent());
				SkeletonConstructor skeletonConstructor = skeletonFactory
						.makeSkeletonConstructor(preElement.text());

				constructors.put(skeletonConstructor, javaDocConstructor);
			}
		}

		return constructors;
	}

	/**
	 * Extract constructor from given element.
	 * 
	 * @param blistelement
	 *            Element {@code blocklist} which contains information about
	 *            constructor.
	 * @return New instance of {@code JavaDocMethod} (construcotr).
	 */
	private JavaDocMethod extractJavaDocConstructor(Element blistelement) {

		String[] absJavaDoc = extractAbstractJavaDoc(blistelement);

		List<String> parameters = extractMethodParam(blistelement);

		return javaDocFactory.makeJavaDocMethod(absJavaDoc[0], absJavaDoc[1],
				absJavaDoc[2], absJavaDoc[3], absJavaDoc[4], parameters,
				new String());
	}

	/**
	 * Extract method parameters from given String.
	 * 
	 * @param blistelement
	 *            Element containing information about parameters.
	 * @return List of parameters, every parameter is a String containing name -
	 *         type.
	 */
	private List<String> extractMethodParam(Element blistelement) {

		Elements dlElements = blistelement.parent().getElementsByTag("dl");

		List<String> parameters = new ArrayList<String>();

		if (!dlElements.isEmpty()) {

			Elements strongElements = dlElements.first().getElementsByClass(
					"strong");

			for (Element el : strongElements) {

				if (el.text().contains("Parameters")) {

					Elements dlChildren = el.parent().parent().children();

					boolean foundParameters = false;
					for (Element dlChild : dlChildren) {

						if (dlChild.text().contains("Parameters:")) {

							foundParameters = true;
							continue;
						}

						if (!foundParameters) {

							continue;
						}

						if (foundParameters && dlChild.tagName().equals("dt")) {

							break;
						}

						parameters.add(dlChild.text().trim());
					}
				}
			}
		}

		return parameters;
	}

	/**
	 * Extract standard {@code AbstractJavaDoc} parameters.
	 * 
	 * @param blistelement
	 *            Block list element.
	 * @return An array of Strings.
	 */
	private String[] extractAbstractJavaDoc(Element blistelement) {

		String name = "";
		String comment = "";
		String deprecated = "";
		String since = "";
		String see = "";

		name = blistelement.getElementsByTag("h4").text().trim();

		Elements blockElements = blistelement.getElementsByClass("block");

		for (Element blockElement : blockElements) {

			if (blockElement.text().contains("Deprecated.")) {

				deprecated = blockElement.text().trim();

			} else {

				comment = blockElement.text().trim();
			}
		}

		Elements dlElements = blistelement.getElementsByTag("dl");

		if (!dlElements.isEmpty()) {

			Elements strongElements = dlElements.first().getElementsByClass(
					"strong");

			for (Element el : strongElements) {

				if (el.text().contains("Since")) {

					since = el.parent().nextElementSibling().text().trim();
				} else if (el.text().contains("See")) {

					see = el.parent().nextElementSibling().text().trim();
				}
			}
		}

		// this order has to be met up.
		return new String[] { name, comment, deprecated, since, see };
	}

	/**
	 * Extracts all methods from given jsoup document.
	 * 
	 * @param jsoupDocument
	 *            Reference to an jsoup document.
	 * @return HashMap with pairs method - it's javadoc.
	 */
	private Map<SkeletonMethod, JavaDocMethod> extractMethods(
			Document jsoupDocument) {

		Map<SkeletonMethod, JavaDocMethod> methods = new HashMap<SkeletonMethod, JavaDocMethod>();

		Elements methodDetailElements = jsoupDocument
				.getElementsByAttributeValue("name", "method_detail");

		if (!methodDetailElements.isEmpty()) {

			Elements h4Elements = methodDetailElements.parents().first()
					.getElementsByTag("h4");

			for (Element h4element : h4Elements) {

				JavaDocMethod javaDocMethod = extractJavaDocMethod(h4element
						.parent());
				SkeletonMethod skeletonMethod = skeletonFactory
						.makeSkeletonMethod(h4element.nextElementSibling()
								.text());

				methods.put(skeletonMethod, javaDocMethod);
			}

		}

		return methods;
	}

	/**
	 * Extract one {@code JavaDocMethod} from an element.
	 * 
	 * @param blistelement
	 *            Element from which is JavaDocMethod being parsed.
	 * @return New instance of {@code JavaDocMethod}.
	 */
	private JavaDocMethod extractJavaDocMethod(Element blistelement) {

		String[] absJavaDoc = extractAbstractJavaDoc(blistelement);

		List<String> parameters = extractMethodParam(blistelement);

		Elements dlElements = blistelement.getElementsByTag("dl");

		String returns = "";

		if (!dlElements.isEmpty()) {

			Elements strongElements = dlElements.first().getElementsByClass(
					"strong");

			for (Element el : strongElements) {

				if (el.text().contains("Returns")) {

					returns = el.parent().nextElementSibling().text().trim();
				}
			}
		}

		return javaDocFactory.makeJavaDocMethod(absJavaDoc[0], absJavaDoc[1],
				absJavaDoc[2], absJavaDoc[3], absJavaDoc[4], parameters,
				returns);
	}

	/**
	 * Extracts all attributes from given jsoup document.
	 * 
	 * @param jsoupDocument
	 *            Reference to jsoup documents.
	 * @return Hashmap with pairs attribute - it's javadoc.
	 */
	private Map<SkeletonAttribute, JavaDocAttribute> extractAttributes(
			Document jsoupDocument) {

		Map<SkeletonAttribute, JavaDocAttribute> attributes = new HashMap<SkeletonAttribute, JavaDocAttribute>();

		Elements fieldDetailElements = jsoupDocument
				.getElementsByAttributeValue("name", "field_detail");

		if (!fieldDetailElements.isEmpty()) {

			Elements preElements = fieldDetailElements.parents().first()
					.getElementsByTag("pre");

			for (Element preElement : preElements) {

				SkeletonAttribute skeletonAttribute = skeletonFactory
						.makeSkeletonAttribute(preElement.text());
				JavaDocAttribute javaDocAttribute = extractJavaDocAttribut(preElement
						.parent());

				attributes.put(skeletonAttribute, javaDocAttribute);
			}
		}

		return attributes;
	}

	/**
	 * Extract one {@code JavaDocAttribute} from an element.
	 * 
	 * @param blistelement
	 *            Reference to element, from which is {@code JavaDocAttribute}
	 *            parsed.
	 * @return New instance of {@code JavaDocAttribute}.
	 */
	private JavaDocAttribute extractJavaDocAttribut(Element blistelement) {

		String[] absJavaDoc = extractAbstractJavaDoc(blistelement);

		return javaDocFactory.makeJavaDocAttribute(absJavaDoc[0],
				absJavaDoc[1], absJavaDoc[2], absJavaDoc[3], absJavaDoc[4]);
	}
	
	static String readFile(String path, Charset encoding) 
			  throws IOException 
			{
			  byte[] encoded = Files.readAllBytes(Paths.get(path));
			  return new String(encoded, encoding);
			}

}
