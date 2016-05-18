base_javadoc_url="http://cogcomp.cs.illinois.edu/software/doc/illinois-edison/apidocs/"

wget -nH -r --no-parent $base_javadoc_url 
rm -r software/doc/illinois-edison/apidocs/index.html?*
rm -r src/test/apidoc/*
mv software src/test/apidoc/ 