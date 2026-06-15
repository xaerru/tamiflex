package de.bodden.tamiflex.playin;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ARETURN;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.POP;
import static org.objectweb.asm.Opcodes.IRETURN;
import static org.objectweb.asm.Opcodes.RETURN;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.ASM9;
import static org.objectweb.asm.Opcodes.PUTFIELD;
import static org.objectweb.asm.Opcodes.GETFIELD;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.SWAP;
import static org.objectweb.asm.Opcodes.ILOAD;


public class HiddenClassTransformer implements ClassFileTransformer {

    @Override
    public byte[] transform(ClassLoader ldr, String className, Class<?> classBeingRedefined, 
                            ProtectionDomain protectionDomain, byte[] classfileBuffer) 
                            throws IllegalClassFormatException {
        try {
            if (!className.startsWith("java/lang/invoke/MethodHandles$Lookup")) {
                return null; // Return null means "no changes, use original"
            }

            ClassReader cr = new ClassReader(classfileBuffer);

            ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_FRAMES);

            ClassVisitor cv = new HiddenClassModificationVisitor(Opcodes.ASM9, cw);

            cr.accept(cv, ClassReader.EXPAND_FRAMES);

            return cw.toByteArray();

        } catch (Throwable e) {
            e.printStackTrace();
            throw e;
        }
    }

    static class HiddenClassModificationVisitor extends ClassVisitor {

        public HiddenClassModificationVisitor(int api, ClassVisitor classVisitor) {
            super(api, classVisitor);
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String descriptor, 
                                         String signature, String[] exceptions) {
            MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);

            if ("makeHiddenClassDefiner".equals(name)) {
                return new HiddenClassMethodAdapter(api, mv);
            }
            return mv;
        }
    }

    static class HiddenClassMethodAdapter extends MethodVisitor {
        public HiddenClassMethodAdapter(int api, MethodVisitor methodVisitor) {
            super(api, methodVisitor);
        }

        @Override
        public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
            if (opcode == INVOKESPECIAL &&
                name.equals("<init>") &&
                owner.equals("java/lang/invoke/MethodHandles$Lookup$ClassDefiner")) {
                // Stack: [..., ClassFile, int (flags), ClassFileDumper]

                super.visitInsn(POP);
                super.visitInsn(POP);
                // Stack: [..., ClassFile]

                super.visitInsn(DUP);
                // Stack: [ClassFile, ClassFile]

                super.visitFieldInsn(GETFIELD, "java/lang/invoke/MethodHandles$Lookup$ClassFile", "name", "Ljava/lang/String;");
                // Stack: [ClassFile, String (name)]

                super.visitInsn(SWAP);
                // Stack: [String (name), ClassFile]

                super.visitFieldInsn(GETFIELD, "java/lang/invoke/MethodHandles$Lookup$ClassFile", "bytes", "[B");
                // Stack: [String (name), byte[] (oldBytes)]

                super.visitMethodInsn(INVOKESTATIC,
                    "de/bodden/tamiflex/playin/rt/HiddenClassLoader",
                    "loadHiddenClass",
                    "([B)[B",
                    false);
                // Stack: [String (name), byte[] (newBytes)]

                super.visitMethodInsn(INVOKESTATIC,
                    "java/lang/invoke/MethodHandles$Lookup$ClassFile",
                    "newInstanceNoCheck",
                    "(Ljava/lang/String;[B)Ljava/lang/invoke/MethodHandles$Lookup$ClassFile;",
                    false);
                // Stack: [..., NewClassFile]

                super.visitVarInsn(ILOAD, 5); // Reload flags
                super.visitVarInsn(ALOAD, 4); // Reload dumper

                // Stack: [..., NewClassFile, int (flags), ClassFileDumper]
            }
            super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
        };
    }
}
