package de.bodden.tamiflex.playout.transformation.invoke;

import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ARETURN;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.POP;
import static org.objectweb.asm.Opcodes.IRETURN;
import static org.objectweb.asm.Opcodes.RETURN;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.ASM9;

import java.lang.invoke.LambdaMetafactory;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.commons.Method;

import de.bodden.tamiflex.playout.transformation.AbstractTransformation;
import de.bodden.tamiflex.playout.Agent;


// Modify InnerClassLambdaMetafactory to dump the class bytes to disk after it is generated
public abstract class AbstractLambdaTransformation extends AbstractTransformation {
	
	public AbstractLambdaTransformation(Method... methods) throws Exception {
		super(Class.forName("java.lang.invoke.InnerClassLambdaMetafactory"), methods);
	}
	
	@Override
	protected MethodVisitor getMethodVisitor(MethodVisitor parent) 
    {
        return new MethodVisitor(ASM9, parent) {
			
            @Override
            public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
                super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);

                // Some classes that get dumped with -Djdk.internal.lambda.dumpProxyClasses=dir/ don't get dumped with this instrumentation
                // For example: jdk/internal/loader/BootLoader$PackageHelper$$Lambda$1.class
                // When run with dacapo-23.11-chopin.jar avrora -s small

                // For OpenJ9 25
                // if (owner.equals("java/lang/classfile/ClassFile") && name.equals("build")) {
                // For OpenJ9 21
                if (owner.equals("jdk/internal/org/objectweb/asm/ClassWriter") && name.equals("toByteArray")) {
                    super.visitLdcInsn(Agent.getOutPath());
                    super.visitMethodInsn(INVOKESTATIC, "de/bodden/tamiflex/playout/rt/ReflLogger", methodName(), methodSignature(), false);
                }
			}

		};
	}
	protected abstract String methodName();

	protected abstract String methodSignature();
}
