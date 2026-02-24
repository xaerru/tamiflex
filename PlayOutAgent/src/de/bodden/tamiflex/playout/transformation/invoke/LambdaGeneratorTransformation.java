package de.bodden.tamiflex.playout.transformation.invoke;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.Method;

public class LambdaGeneratorTransformation extends AbstractLambdaTransformation {
	
	public LambdaGeneratorTransformation() throws Exception {
		super(new Method("makeHiddenClassDefiner", "(Ljava/lang/invoke/MethodHandles$Lookup$ClassFile;Ljava/util/Set;ZLjdk/internal/util/ClassFileDumper;)Ljava/lang/invoke/MethodHandles$Lookup$ClassDefiner;"));
	}

	@Override
	protected String methodName() {
		return "dumpLambdaClass";
	}

	@Override
	protected String methodSignature() {
		return "([BLjava/lang/String;)[B";
	}
}
