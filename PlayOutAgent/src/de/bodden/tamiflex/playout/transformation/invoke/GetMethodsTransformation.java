package de.bodden.tamiflex.playout.transformation.invoke;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.Method;

public class GetMethodsTransformation extends AbstractGetMethodsTransformation {
	
	public GetMethodsTransformation() throws Exception {
		super(new Method("getMethods", "()[Ljava/lang/reflect/Method;"));
	}

	@Override
	protected String methodName() {
		return "sortMethods";
	}

	@Override
	protected String methodSignature() {
		return "([Ljava/lang/reflect/Method;)[Ljava/lang/reflect/Method;";
	}
}
