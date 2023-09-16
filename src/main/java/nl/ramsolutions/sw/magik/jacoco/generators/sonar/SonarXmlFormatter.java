package nl.ramsolutions.sw.magik.jacoco.generators.sonar;

import org.jacoco.report.IReportVisitor;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Formatter which creates a file following the SonarQube Generic test coverage
 * report format.
 *
 * XSD:
 *
 * <pre>
 * {@code
 * <xs:schema>
 *   <xs:element name="coverage">
 *     <xs:complexType>
 *       <xs:sequence>
 *         <xs:element name="file" minOccurs="0" maxOccurs="unbounded">
 *           <xs:complexType>
 *             <xs:sequence>
 *               <xs:element name="lineToCover" minOccurs="0" maxOccurs="unbounded">
 *                 <xs:complexType>
 *                   <xs:attribute name="lineNumber" type="xs:positiveInteger" use="required"/>
 *                   <xs:attribute name="covered" type="xs:boolean" use="required"/>
 *                   <xs:attribute name="branchesToCover" type="xs:nonNegativeInteger"/>
 *                   <xs:attribute name="coveredBranches" type="xs:nonNegativeInteger"/>
 *                 < /xs:complexType>
 *               < /xs:element>
 *             < /xs:sequence>
 *           <xs:attribute name="path" type="xs:string" use="required"/>
 *           < /xs:complexType>
 *         < /xs:element>
 *       < /xs:sequence>
 *       <xs:attribute name="version" type="xs:positiveInteger" use="required"/>
 *     < /xs:complexType>
 *   < /xs:element>
 * < /xs:schema>
 * }
 * </pre>
 */
public class SonarXmlFormatter {

    /**
     * Create a new visitor.
     *
     * @param output Output stream to write to.
     * @return Vistor.
     * @throws IOException -
     */
    public IReportVisitor createVisitor(final OutputStream output) throws IOException {
        return new SonarXmlVisitor(output);
    }

}
