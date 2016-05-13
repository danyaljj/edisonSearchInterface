jdparser
========

JavaDoc parser (adapted from jdparser - https://github.com/scais/jdparser)

Purpose of this project is to provide simple JavaDoc parser for the Edison search interface JSON ingest.

Installation: To run the project copy base javadoc repository containing the overview-frame.html file. (In our example, we have the directory set to /src/test/javadoc-example/jdparser/apidoc/software/doc/illinois-edison/apidocs/' and that is the directory we would expect our data ingest to be in for this project.

How to generate the data ingest (data.json) for search interface:

Run mvn generate-json after having copied the javadoc folder into the specified directory. This will generate a 'data.json' file in the root directory of the web application.