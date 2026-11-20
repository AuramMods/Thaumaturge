package com.aurammods.thaumaturge.testing;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/** Controls only the instrumented test JVM; never included in the mod distribution. */
public final class BackgroundTestAgent {
    private static final String GLFW = "org/lwjgl/glfw/GLFW";

    private BackgroundTestAgent() {}

    public static void premain(String arguments, Instrumentation instrumentation) {
        boolean hidden = "hidden".equals(arguments);
        boolean interactive = "interactive".equals(arguments);
        if (!interactive) {
            System.setProperty("apple.awt.UIElement", "true");
        }
        instrumentation.addTransformer(new ClassFileTransformer() {
            @Override
            public byte[] transform(ClassLoader loader, String name, Class<?> redefined,
                    ProtectionDomain domain, byte[] bytes) {
                if (!GLFW.equals(name)) {
                    return null;
                }
                return instrumentGlfw(bytes, hidden, interactive);
            }
        });
        System.err.println(interactive
                ? "[background-test] Unfocused startup enabled with normal window input"
                : "[background-test] GLFW focus suppression installed");
    }

    private static byte[] instrumentGlfw(byte[] bytes, boolean hidden, boolean interactive) {
        ClassReader reader = new ClassReader(bytes);
        ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_MAXS);
        reader.accept(new ClassVisitor(Opcodes.ASM9, writer) {
            @Override
            public MethodVisitor visitMethod(int access, String name, String descriptor,
                    String signature, String[] exceptions) {
                MethodVisitor target = super.visitMethod(access, name, descriptor, signature, exceptions);
                if (!interactive && descriptor.equals("(J)V")
                        && (name.equals("glfwFocusWindow") || (hidden && name.equals("glfwShowWindow")))) {
                    target.visitCode();
                    target.visitInsn(Opcodes.RETURN);
                    target.visitMaxs(0, 2);
                    target.visitEnd();
                    return null;
                }
                return new MethodVisitor(Opcodes.ASM9, target) {
                    @Override
                    public void visitCode() {
                        super.visitCode();
                        if (!interactive && name.equals("glfwInit") && descriptor.equals("()Z")) {
                            hint("GLFW_COCOA_MENUBAR", "glfwInitHint");
                        }
                        if (name.equals("nglfwCreateWindow") && descriptor.equals("(IIJJJ)J")) {
                            // Apply after all caller hints, including loading-window hints.
                            if (hidden) {
                                hint("GLFW_VISIBLE", "glfwWindowHint");
                            }
                            hint("GLFW_FOCUSED", "glfwWindowHint");
                            hint("GLFW_FOCUS_ON_SHOW", "glfwWindowHint");
                        }
                    }

                    private void hint(String field, String setter) {
                        super.visitFieldInsn(Opcodes.GETSTATIC, GLFW, field, "I");
                        super.visitInsn(Opcodes.ICONST_0);
                        super.visitMethodInsn(Opcodes.INVOKESTATIC, GLFW, setter, "(II)V", false);
                    }
                };
            }
        }, 0);
        if (interactive) {
            System.err.println("[background-test] Instrumented only GLFW window creation for interactive use");
        } else {
            System.err.println(hidden
                    ? "[background-test] Instrumented GLFW to keep windows hidden and unfocused"
                    : "[background-test] Instrumented GLFW window creation and explicit focus requests");
        }
        return writer.toByteArray();
    }
}
