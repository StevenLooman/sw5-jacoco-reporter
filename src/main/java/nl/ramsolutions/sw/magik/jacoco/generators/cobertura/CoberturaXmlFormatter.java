package nl.ramsolutions.sw.magik.jacoco.generators.cobertura;

import org.jacoco.report.IReportVisitor;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.List;

/**
 * Formatter which creates a file following the {@literal coverage-04.dtd} format.
 *
 * DTD:
 * <pre>
 * {@code
 * <!-- Portions (C) International Organization for Standardization 1986:
 *      Permission to copy in any form is granted for use with
 *      conforming SGML systems and applications as defined in
 *      ISO 8879, provided this notice is included in all copies.
 * -->
 *
 *   <!ELEMENT coverage (sources?,packages)>
 *   <!ATTLIST coverage line-rate        CDATA #REQUIRED>
 *   <!ATTLIST coverage branch-rate      CDATA #REQUIRED>
 *   <!ATTLIST coverage lines-covered    CDATA #REQUIRED>
 *   <!ATTLIST coverage lines-valid      CDATA #REQUIRED>
 *   <!ATTLIST coverage branches-covered CDATA #REQUIRED>
 *   <!ATTLIST coverage branches-valid   CDATA #REQUIRED>
 *   <!ATTLIST coverage complexity       CDATA #REQUIRED>
 *   <!ATTLIST coverage version          CDATA #REQUIRED>
 *   <!ATTLIST coverage timestamp        CDATA #REQUIRED>
 *
 *   <!ELEMENT sources (source*)>
 *
 *   <!ELEMENT source (#PCDATA)>
 *
 *   <!ELEMENT packages (package*)>
 *
 *   <!ELEMENT package (classes)>
 *   <!ATTLIST package name        CDATA #REQUIRED>
 *   <!ATTLIST package line-rate   CDATA #REQUIRED>
 *   <!ATTLIST package branch-rate CDATA #REQUIRED>
 *   <!ATTLIST package complexity  CDATA #REQUIRED>
 *
 *   <!ELEMENT classes (class*)>
 *
 *   <!ELEMENT class (methods,lines)>
 *   <!ATTLIST class name        CDATA #REQUIRED>
 *   <!ATTLIST class filename    CDATA #REQUIRED>
 *   <!ATTLIST class line-rate   CDATA #REQUIRED>
 *   <!ATTLIST class branch-rate CDATA #REQUIRED>
 *   <!ATTLIST class complexity  CDATA #REQUIRED>
 *
 *   <!ELEMENT methods (method*)>
 *
 *   <!ELEMENT method (lines)>
 *   <!ATTLIST method name        CDATA #REQUIRED>
 *   <!ATTLIST method signature   CDATA #REQUIRED>
 *   <!ATTLIST method line-rate   CDATA #REQUIRED>
 *   <!ATTLIST method branch-rate CDATA #REQUIRED>
 *   <!ATTLIST method complexity  CDATA #REQUIRED>
 *
 *   <!ELEMENT lines (line*)>
 *
 *   <!ELEMENT line (conditions*)>
 *   <!ATTLIST line number CDATA #REQUIRED>
 *   <!ATTLIST line hits   CDATA #REQUIRED>
 *   <!ATTLIST line branch CDATA "false">
 *   <!ATTLIST line condition-coverage CDATA "100%">
 *
 *   <!ELEMENT conditions (condition*)>
 *
 *   <!ELEMENT condition EMPTY>
 *   <!ATTLIST condition number CDATA #REQUIRED>
 *   <!ATTLIST condition type CDATA #REQUIRED>
 *   <!ATTLIST condition coverage CDATA #REQUIRED>
 * }
 * </pre>
 */
public class CoberturaXmlFormatter {

    /**
     * Create a new visitor.
     * @param output Output stream to write to.
     * @return Vistor.
     * @throws IOException -
     */
    public IReportVisitor createVisitor(final OutputStream output, final List<Path> sourcePaths) throws IOException {
        return new CoberturaXmlVisitor(output, sourcePaths);
    }

}
