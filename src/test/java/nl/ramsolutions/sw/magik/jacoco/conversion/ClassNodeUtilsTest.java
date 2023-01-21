package nl.ramsolutions.sw.magik.jacoco.conversion;

import org.junit.jupiter.api.Test;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.NoSuchElementException;

import nl.ramsolutions.sw.magik.jacoco.ClassNodeHelper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Test cases for ClassNodeUtils.
 */
class ClassNodeUtilsTest {

    @Test
    void testGetMethodFound() {
        final ClassNode classNode = new ClassNode();

        final MethodNode methodNode0 = new MethodNode(0, "m0", "", "", new String[0]);
        classNode.methods.add(methodNode0);

        final MethodNode methodNode1 = new MethodNode(0, "m1", "", "", new String[0]);
        classNode.methods.add(methodNode1);

        final MethodNode actual = ClassNodeHelper.getMethodNodeFromClassNode(classNode, "m1");
        assertThat(actual).isEqualTo(methodNode1);
    }

    @Test
    void testGetMethodNotFound() {
        final ClassNode classNode = new ClassNode();

        assertThatThrownBy(
            () -> ClassNodeHelper.getMethodNodeFromClassNode(classNode, "m99"))
                .isInstanceOf(NoSuchElementException.class);
    }

}
