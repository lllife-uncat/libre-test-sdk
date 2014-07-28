package com.systems.ko;

import com.sun.star.uno.UnoRuntime;

import java.io.File;

/**
 * Created by wk on 7/28/14 AD.
 */
public class DocumentConverter {

    /** Containing the loaded documents
     */
    static com.sun.star.frame.XComponentLoader xCompLoader = null;
    /** Containing the given type to convert to
     *
     */
    static String sConvertType = "";
    /** Containing the given extension
     */
    static String sExtension = "";
    /** Containing the current file or directory
     */
    static String sIndent = "";
    /** Containing the directory where the converted files are saved
     */
    static String sOutputDir = "";

    /** Traversing the given directory recursively and converting their files to
     * the favoured type if possible
     * @param fileDirectory Containing the directory
     */
    static void traverse( File fileDirectory ) {
        // Testing, if the file is a directory, and if so, it throws an exception
        if ( !fileDirectory.isDirectory() ) {
            throw new IllegalArgumentException(
                    "not a directory: " + fileDirectory.getName()
            );
        }

        // Prepare Url for the output directory
        File outdir = new File(DocumentConverter.sOutputDir);
        String sOutUrl = "file:///" + outdir.getAbsolutePath().replace( '\\', '/' );

        System.out.println("\nThe converted documents will stored in \""
                + outdir.getPath() + "!");

        System.out.println(sIndent + "[" + fileDirectory.getName() + "]");
        sIndent += "  ";

        // Getting all files and directories in the current directory
        File[] entries = fileDirectory.listFiles();


        // Iterating for each file and directory
        for ( int i = 0; i < entries.length; ++i ) {
            // Testing, if the entry in the list is a directory
            if ( entries[ i ].isDirectory() ) {
                // Recursive call for the new directory
                traverse( entries[ i ] );
            } else {
                // Converting the document to the favoured type
                try {
                    // Composing the URL by replacing all backslashs
                    String sUrl = "file:///"
                            + entries[ i ].getAbsolutePath().replace( '\\', '/' );

                    if(!sUrl.endsWith("odt")) continue;
                    if(sUrl.contains("~")) continue;

                    // Loading the wanted document
                    com.sun.star.beans.PropertyValue propertyValues[] =
                            new com.sun.star.beans.PropertyValue[1];
                    propertyValues[0] = new com.sun.star.beans.PropertyValue();
                    propertyValues[0].Name = "Hidden";
                    propertyValues[0].Value = new Boolean(true);

                    Object oDocToStore =
                            DocumentConverter.xCompLoader.loadComponentFromURL(
                                    sUrl, "_blank", 0, propertyValues);

                    // Getting an object that will offer a simple way to store
                    // a document to a URL.
                    com.sun.star.frame.XStorable xStorable =
                            UnoRuntime.queryInterface(
                                    com.sun.star.frame.XStorable.class, oDocToStore);

                    // Preparing properties for converting the document
                    propertyValues = new com.sun.star.beans.PropertyValue[2];
                    // Setting the flag for overwriting
                    propertyValues[0] = new com.sun.star.beans.PropertyValue();
                    propertyValues[0].Name = "Overwrite";
                    propertyValues[0].Value = new Boolean(true);
                    // Setting the filter name
                    propertyValues[1] = new com.sun.star.beans.PropertyValue();
                    propertyValues[1].Name = "FilterName";
                    propertyValues[1].Value = DocumentConverter.sConvertType;

                    // Appending the favoured extension to the origin document name
                    int index1 = sUrl.lastIndexOf('/');
                    int index2 = sUrl.lastIndexOf('.');
                    String sStoreUrl = sOutUrl + sUrl.substring(index1, index2 + 1)
                            + DocumentConverter.sExtension;

                    // Storing and converting the document
                    xStorable.storeAsURL(sStoreUrl, propertyValues);

                    // Closing the converted document. Use XCloseable.clsoe if the
                    // interface is supported, otherwise use XComponent.dispose
                    com.sun.star.util.XCloseable xCloseable =
                            UnoRuntime.queryInterface(
                                    com.sun.star.util.XCloseable.class, xStorable);

                    if ( xCloseable != null ) {
                        xCloseable.close(false);
                    } else {
                        com.sun.star.lang.XComponent xComp =
                                UnoRuntime.queryInterface(
                                        com.sun.star.lang.XComponent.class, xStorable);

                        xComp.dispose();
                    }
                }
                catch( Exception e ) {
                    e.printStackTrace(System.err);
                }

                System.out.println(sIndent + entries[ i ].getName());
            }
        }

        sIndent = sIndent.substring(2);
    }

    public static boolean convert() {

        //String resource = Paths.get(".").toAbsolutePath().toString();
        String resource = ClassLoader.getSystemResource(".").getPath();
        String inputDir = new File(resource, "input").getAbsolutePath();
        String outputDir = new File(resource, "output").getAbsolutePath();

        //String type = "OpenDocument Text Flat XML";
        String type = "OpenDocument Text Flat XML";
        type = "Text";
        String extension = "txt";

        System.out.printf("input: %s\n", inputDir);
        System.out.printf("output: %s\n", outputDir);

        String[] args = new String[] { inputDir, type, extension, outputDir };
        start(args);

        return true;
    }

    /** Bootstrap UNO, getting the remote component context, getting a new instance
     * of the desktop (used interface XComponentLoader) and calling the
     * static method traverse
     * @param args The array of the type String contains the directory, in which
     *             all files should be converted, the favoured converting type
     *             and the wanted extension
     */
    public static void main( String args[] ) {
        if (args.length < 3) {
            System.out.println("usage: java -jar DocumentConverter.jar " +
                    "\"<directory to convert>\" \"<type to convert to>\" " +
                    "\"<extension>\" \"<output_directory>\"");
            System.out.println("\ne.g.:");
            System.out.println("usage: java -jar DocumentConverter.jar " +
                    "\"c:/myoffice\" \"swriter: MS Word 97\" \"doc\"");
            System.exit(1);
        }

        start(args);
    }

    private static void start(String args[]){

        com.sun.star.uno.XComponentContext xContext = null;

        try {
            // get the remote office component context
            xContext = com.sun.star.comp.helper.Bootstrap.bootstrap();

            //String sofficePath = "/Applications/LibreOffice.app/Contents/MacOS";
            //xContext = com.sun.star.comp.helper.Bootstrap.bootstrap();

            System.out.println("Connected to a running office ...");

            // get the remote office service manager
            com.sun.star.lang.XMultiComponentFactory xMCF =
                    xContext.getServiceManager();

            Object oDesktop = xMCF.createInstanceWithContext(
                    "com.sun.star.frame.Desktop", xContext);

            xCompLoader = UnoRuntime.queryInterface(com.sun.star.frame.XComponentLoader.class,
                    oDesktop);

            // Getting the given starting directory
            File file = new File(args[0]);

            // Getting the given type to convert to
            sConvertType = args[1];

            // Getting the given extension that should be appended to the
            // origin document
            sExtension = args[2];

            // Getting the given type to convert to
            sOutputDir = args[3];

            // Starting the conversion of documents in the given directory
            // and subdirectories
            traverse(file);

            //System.exit(0);
        } catch( Exception e ) {
            e.printStackTrace(System.err);
            System.exit(1);
        }
    }
}
