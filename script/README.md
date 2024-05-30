This script migrates the WHO-FIC Foundation project from the current iCAT SQL database dump into an OWL 2.0 file, which will be used to edit the Foundation in iCAT-X. 

NOTE: Even though, this script builds on the iCAT OWL 2.0 export script, they are different, and have different purposes. This script will generate the complete Foundation content that will be used in iCAT-X, and is not intended for other purposes. For a simplified OWL 2.0 export of the iCAT Content, use the OWL Export script at:
https://github.com/protegeproject/icd-owl-export

----- To use this script -----

Edit export.properties to match your configuration (e.g., project paths, output path, max heap size, etc.). Each property has a short description for the expected value.

The script will create the output file {output.owl.file} as configured in export.properties. This generated OWL file will import the content model file {cm.owl}, which in turn imports the postcoordination properties OWL file {cm-postcoordination.owl} from {script/contentmodel} folder.


Run the script via the command:

 ant run

The output will be in the {output.owl.file} as configured in export.properties.


The script may take 30 mins to run, so it is recommended to be run in a screen session, if run on a Linux server. See more: https://linux.die.net/man/1/screen

