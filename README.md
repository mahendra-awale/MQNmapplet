MQNmapplet
==========

Visualization of Chemical Space

######################################################
The MQNmapplet is a Java application giving access to the structure of small molecules in large databases via color-coded maps of their chemical space. These maps are projections from a 42-dimensional property space defined by 42 integer value descriptors called molecular quantum numbers (MQN), which count different categories of atoms, bonds, polar groups, and topological features and categorize molecules by size, rigidity, and polarity. Despite its simplicity, MQN-space is relevant to biological activities. The MQN-mapplet allows localization of any molecule on the color-coded images, visualization of the molecules, and identification of analogs as neighbors on the MQN-map or in the original 42-dimensional MQN-space. No query molecule is necessary to start the exploration, which may be particularly attractive for nonchemists. To our knowledge, this type of interactive exploration tool is unprecedented for very large databases such as PubChem and GDB-13 (almost one billion molecules). The application is freely available for download at www.gdb.unibe.ch.

######################################################

This part of the software provide the visualization capability and its highly depend upon the data format. Data for visualization was generated seperately (using different code) which one can find in the dbases folder in this project. Within this project the generated data files provided for three database:

ZINC=     13.5 Millions commercially availaible compounds (http://zinc.docking.org)
PubChem=  Public database of small molecule (https://pubchem.ncbi.nlm.nih.gov/)
GDB-13=   All possible molecules upto heavy atom count of 13(http://reymond.dcb.unibe.ch/)

For each of the database only average molecule file is provided, while access to the rest of molecules is enabled by "show bin option" whihc retrived the data from our in-house server.

For further details please contact us and we will be happy to help you out!
######################################################

Dependancy:

JCHEM java library from ChemAxon (http://www.chemaxon.com/) and JSCI library (http://jsci.sourceforge.net/api/JSci/maths/package-summary.html) needs to be in the classpath.

######################################################
