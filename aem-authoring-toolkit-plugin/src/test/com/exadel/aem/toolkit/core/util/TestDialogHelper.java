package com.exadel.aem.toolkit.core.util;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.exadel.aem.toolkit.api.annotations.editconfig.EditConfig;

public class TestDialogHelper {
    private static final Logger LOG = LoggerFactory.getLogger(TestDialogHelper.class);

    private TestDialogHelper() {
    }

    public static boolean testDialogAnnotation(String testedClass, Path pathToExpectedFiles) throws ClassNotFoundException {
        Class dialogClass = Class.forName(com.exadel.aem.toolkit.core.util.TestsConstants.TESTCASE_PACKAGE + "." + testedClass);
        List<PackageEntryWriter> writers = getWriters(dialogClass);

        Map<String, InputStream> actualFiles = getActualFiles(dialogClass, writers);
        Map<String, InputStream> expectedFiles = TestDialogHelper.getFilesUnderPath(pathToExpectedFiles);

        return TestDialogHelper.testEqualsFiles(actualFiles, expectedFiles);
    }

    private static Map<String, InputStream> getActualFiles(Class dialogClass, List<PackageEntryWriter> writers) {
        Map<String, InputStream> actualFiles = new HashMap<>();

        writers.forEach(packageEntryWriter -> {
            StringWriter stringWriter = new StringWriter();
            packageEntryWriter.writeXml(dialogClass, stringWriter);
            actualFiles.put(packageEntryWriter.getXmlScope().toString(), new ByteArrayInputStream(stringWriter.toString().getBytes()));
        });

        return actualFiles;
    }

    private static List<PackageEntryWriter> getWriters(Class dialogClass) {
        List<PackageEntryWriter> writers = new ArrayList<>();
        try {
            DocumentBuilder documentBuilder = PackageWriter.createDocumentBuilder();
            Transformer transformer = PackageWriter.createTransformer();
            writers.add(new ContentXmlWriter(documentBuilder, transformer));
            writers.add(new CqDialogWriter(documentBuilder, transformer));

            if (dialogClass.isAnnotationPresent(EditConfig.class)) {
                writers.add(new CqEditConfigWriter(documentBuilder, transformer));
            }
        } catch (ParserConfigurationException | TransformerConfigurationException e) {
            LOG.error(e.getMessage());
        }

        return writers;
    }

    private static Map<String, InputStream> getFilesUnderPath(Path componentsPath) {
        Map<String, InputStream> actualFiles = new HashMap<>();
        try (Stream<Path> files = Files.walk(componentsPath)){
            files.forEach(path -> {
                try {
                    actualFiles.put(path.getFileName().toString(), new FileInputStream(path.toString()));
                } catch (FileNotFoundException ex) {
                    LOG.error("Can't find file: " + path.getFileName(), ex);
                }
            });
            return actualFiles;
        } catch (IOException ex) {
            LOG.error("Can't read the package with path: " + componentsPath, ex);
            return null;
        }
    }

    private static boolean testEqualsFiles(Map<String, InputStream> actualFiles, Map<String, InputStream> expectedFiles) {
        List<Boolean> testFiles = new ArrayList<>();
        if(expectedFiles == null || expectedFiles.size() != actualFiles.size()) {
            return false;
        } else {
            expectedFiles.forEach((fileName, expectedInputStream) -> {
                InputStream actualInputStream = actualFiles.get(fileName);
                testFiles.add(FilesComparator.compareXMLFiles(actualInputStream, expectedInputStream));
            });
        }
        return !testFiles.contains(Boolean.FALSE);
    }
}
