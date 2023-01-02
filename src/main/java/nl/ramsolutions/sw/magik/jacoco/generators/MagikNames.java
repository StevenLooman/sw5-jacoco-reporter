package nl.ramsolutions.sw.magik.jacoco.generators;

import java.util.Map;
import nl.ramsolutions.sw.magik.jacoco.sw5lib.Sw5LibAnalyzer;
import nl.ramsolutions.sw.magik.jacoco.sw5lib.Sw5LibReader;
import org.jacoco.report.ILanguageNames;
import org.jacoco.report.JavaNames;

/**
 * Magik names extractor for JaCoCo report generator.
 */
public class MagikNames implements ILanguageNames {

    private final Sw5LibAnalyzer libAnalyzer;
    private final JavaNames javaNames;

    public MagikNames(final Sw5LibReader libReader) {
        this.libAnalyzer = new Sw5LibAnalyzer(libReader);
        this.javaNames = new JavaNames();
    }

    @Override
    public String getPackageName(final String vmname) {
        return this.javaNames.getPackageName(vmname);
    }

    @Override
    public String getClassName(
            final String vmname,
            final String vmsignature,
            final String vmsuperclass,
            final String[] vminterfaces) {
        return this.javaNames.getClassName(vmname, vmsignature, vmsuperclass, vminterfaces);
    }

    @Override
    public String getQualifiedClassName(final String vmname) {
        return vmname.replace('/', '.').replace('$', '.');
    }

    @Override
    public String getMethodName(
            final String vmclassname,
            final String vmmethodname,
            final String vmdesc,
            final String vmsignature) {
        // Perhaps we would want some caching...
        Map<String, String> methodNameMapping = this.libAnalyzer.extractAllMethodNames();
        String javaMethodName = this.libAnalyzer.keyForClassMethodName(vmclassname, vmmethodname);
        final String methodName = methodNameMapping.get(javaMethodName);
        if (methodName != null) {
            return methodName;
        }

        return this.javaNames.getMethodName(vmclassname, vmmethodname, vmdesc, vmsignature);
    }

    @Override
    public String getQualifiedMethodName(
            final String vmclassname,
            final String vmmethodname,
            final String vmdesc,
            final String vmsignature) {
        final String qualifiedClassName = this.getQualifiedClassName(vmclassname);
        final String methodName = this.getMethodName(vmclassname, vmmethodname, vmdesc, vmsignature);
        return String.format("%s.%s", qualifiedClassName, methodName);
    }

}
