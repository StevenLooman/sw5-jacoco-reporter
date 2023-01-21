package nl.ramsolutions.sw.magik.jacoco.generators;

import org.jacoco.report.ILanguageNames;
import org.jacoco.report.JavaNames;

/**
 * Magik names extractor for JaCoCo report generator.
 */
public class MagikNames implements ILanguageNames {

    private final JavaNames javaNames;

    public MagikNames() {
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
        return vmmethodname;
    }

    @Override
    public String getQualifiedMethodName(
            final String vmclassname,
            final String vmmethodname,
            final String vmdesc,
            final String vmsignature) {
        final String qualifiedClassName = this.getQualifiedClassName(vmclassname);
        final String methodName = this.getMethodName(vmclassname, vmmethodname, vmdesc, vmsignature);
        if (methodName.startsWith("[")) {
            return String.format("%s%s", qualifiedClassName, methodName);
        }

        return String.format("%s.%s", qualifiedClassName, methodName);
    }

}
