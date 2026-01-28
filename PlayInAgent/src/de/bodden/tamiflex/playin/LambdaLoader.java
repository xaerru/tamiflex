package de.bodden.tamiflex.playin;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.DUP;

public class LambdaLoader implements ClassFileTransformer {

    @Override
    public byte[] transform(ClassLoader ldr, String className, Class<?> classBeingRedefined, 
                            ProtectionDomain protectionDomain, byte[] classfileBuffer) 
                            throws IllegalClassFormatException {
        try {
            if (!className.startsWith("java/lang/invoke/InnerClassLambdaMetafactory")) {
                return null; // Return null means "no changes, use original"
            }

            ClassReader cr = new ClassReader(classfileBuffer);

            ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_FRAMES);

            ClassVisitor cv = new LambdaModificationVisitor(Opcodes.ASM9, cw);

            cr.accept(cv, ClassReader.EXPAND_FRAMES);

            return cw.toByteArray();

        } catch (Throwable e) {
            e.printStackTrace();
            throw e;
        }
    }

    static class LambdaModificationVisitor extends ClassVisitor {

        public LambdaModificationVisitor(int api, ClassVisitor classVisitor) {
            super(api, classVisitor);
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String descriptor, 
                                         String signature, String[] exceptions) {
            MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);

            if ("generateInnerClass".equals(name)) {
                return new LambdaMethodAdapter(api, mv);
            }
            return mv;
        }
    }

    static class LambdaMethodAdapter extends MethodVisitor {
        public LambdaMethodAdapter(int api, MethodVisitor methodVisitor) {
            super(api, methodVisitor);
        }

        @Override
        public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
            super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);

            if (owner.equals("jdk/internal/org/objectweb/asm/ClassWriter") && name.equals("toByteArray")) {
                // byte[] is on stack
                // Push "out" string
                // Use the return value of invokestatic
                // TODO: pass path here
                super.visitLdcInsn("out");
                super.visitMethodInsn(INVOKESTATIC, "de/bodden/tamiflex/playin/rt/Helper", "getExistingLambda", "([BLjava/lang/String;)[B", false);
            }
        }
    }
}
