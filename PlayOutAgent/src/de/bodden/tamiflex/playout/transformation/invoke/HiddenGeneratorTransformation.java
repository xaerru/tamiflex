package de.bodden.tamiflex.playout.transformation.invoke;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.Method;

public class HiddenGeneratorTransformation extends AbstractHiddenTransformation {
	
	public HiddenGeneratorTransformation() throws Exception {
		super(new Method("makeHiddenClassDefiner", "(Ljava/lang/invoke/MethodHandles$Lookup$ClassFile;Ljava/util/Set;ZLjdk/internal/util/ClassFileDumper;)Ljava/lang/invoke/MethodHandles$Lookup$ClassDefiner;"));
	}

	@Override
	protected String methodName() {
		return "dumpHiddenClass";
	}

	@Override
	protected String methodSignature() {
		return "([B)[B";
	}
}
