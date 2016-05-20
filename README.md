# Edison Search Interface

The Edison Web interface is a web application that allows one to search the description and keywords of feature extractors and preview their test code to see how they had been built. More details can be found in [this paper](http://cogcomp.cs.illinois.edu/page/publication_view/791). 

## Technologies being used within Web Application: 
 - jquery
 - jade
 - bootstrap
 - [jquery-searchable](https://github.com/stidges/jquery-searchable)

## Running the application 

This application requires the following to generate its components and to run:
   - node
   - npm (node package manager)
   - bash
   - java (version > 1.6)
   - maven 
   - regular web browser (Safari, FireFox, Google Chrome, etc..)
   
Steps to run the application:

1) Extract the javadoc from the online javadoc repository. 
   - Replace the `base_javadoc_url` variable with the appropriate `base_url` (The url that produces the API overview page)
   - The current default is: `http://cogcomp.cs.illinois.edu/software/doc/illinois-edison/apidocs/`
   - Run `./extract_javadoc.sh` from the `javadoc_scraper` directory

2) Use the jdparser maven package to parser and generate the `data.json` file that will be ingested and presented by the web interface.
   - Ensure that the base_url variable in the `src/test/java/cz/zcu/validator/jdparser/JDParserTest.java` file contains the base url with the root package path of the LREC feature classes.
   - The current default is: `http://cogcomp.cs.illinois.edu/software/doc/apidocs/edu/illinois/cs/cogcomp/edison/features/`
   - Run `mvn test` from the `javadoc_scraper` directory, and this will generate the `data.json` file in the base directory of our web application

3) Start the Web Application 
 - Run `npm install` to install all the web application package dependencies
 - Run `node app`
 - In your browser open: `http://localhost:3000`
 
