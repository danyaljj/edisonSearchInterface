# Edison Search Interface

The Edison Web interface is a Node-based web application that allows one to search the description and keywords of LREC reference implementation feature extractors and preview their test code to see how they had been built.  

This application requires the following to generate its components and to run:
   - node
   - npm (node package manager - version >= 3.8.3)
   - bash
   - java (version > 1.6)
   - mvn
   - regular web browser (Safari, FireFox, Google Chrome, etc..)
   
Steps to run the application:

1) Extract the javadoc from the online javadoc repository. 
   - Replace the base_javadoc_url variable in the 'extract_javadoc.sh' script in the javadoc_scraper folder with the appropriate base_url (The url that produces the API overview page)
   - The current default is: "http://cogcomp.cs.illinois.edu/software/doc/illinois-edison/apidocs/"
   - Run './extract_javadoc.sh' from the 'javadoc_scraper' directory

2) Use the jdparser maven package to parser and generate the 'data.json' file that will be ingested and presented by the web interface.
   - Ensure that the base_url variable in the 'src/test/java/cz/zcu/validator/jdparser/JDParserTest.java' file contains the base url with the root package path of the LREC feature classes.
   - The current default is: "http://cogcomp.cs.illinois.edu/software/doc/apidocs/edu/illinois/cs/cogcomp/edison/features/"
   - Ensure that the github_url_base_raw variable in the 'src/test/java/cz/zcu/validator/jdparser/JDParserTest.java' file contains the base raw github url from which we would append a "Test[FeatureExtractorName]" to download the Test file to be presented.
   - The current default is: "https://raw.githubusercontent.com/IllinoisCogComp/illinois-cogcomp-nlp/master/edison/src/test/java/edu/illinois/cs/cogcomp/edison/features/lrec/" 
   - Run 'mvn test' from the 'javadoc_scraper' directory, and this will generate the 'data.json' file in the base directory of our web application

3) Start the Web Application 
 - Run `npm install` to install all the web application package dependencies
 - Run `node app`
 - In your browser open: `http://localhost:3000`
 
## Technologies being used within Web Application: 
 - jquery
 - jade
 - bootstrap
 - [jquery-searchable](https://github.com/stidges/jquery-searchable)

##Troubleshooting
 - 'Error: Cannot find module 'npmlog'. 
    - This error usually occurs in outdated versions of npm (typically < 2.5.8) so be sure to run 'sudo npm install npm -g' to fix this issue.
    - This error could also occur in some Mac installations and the best means to resolve the issue in that case is to a well-supported package installer such as 'homebrew' to handle your npm installation on Mac   