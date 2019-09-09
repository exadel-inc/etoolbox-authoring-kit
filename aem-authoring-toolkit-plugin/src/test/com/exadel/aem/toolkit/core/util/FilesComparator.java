package com.exadel.aem.toolkit.core.util;

import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.builder.Input;
import org.xmlunit.diff.Diff;

import java.io.InputStream;

class FilesComparator {
    private FilesComparator() {
    }
    static boolean compareXMLFiles(InputStream actualFile, InputStream expectedFile) {
        Diff diff = DiffBuilder.compare(Input.fromStream(expectedFile))
                .withTest(Input.fromStream(actualFile)).normalizeWhitespace().build();
        return !diff.hasDifferences();
    }
}