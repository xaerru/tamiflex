package de.bodden.tamiflex.playin.rt;

import static de.bodden.tamiflex.normalizer.Hasher.hashedClassNameForGeneratedClassBytes;
import static de.bodden.tamiflex.normalizer.Hasher.generateHashNumber;

import org.objectweb.asm.ClassReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class Helper {

    public static byte[] getExistingLambda(byte[] c, String outPath) {
        ClassReader cr = new ClassReader(c);
        String currentRuntimeName = cr.getClassName();

        String lookupName = currentRuntimeName; 

        File localOutDir = new File(outPath);
        generateHashNumber(currentRuntimeName, c);
        String simpleName = hashedClassNameForGeneratedClassBytes(c);;

        if (lookupName.contains("/")) {
            String packageName = lookupName.substring(0, lookupName.lastIndexOf('/'));
            simpleName = simpleName.substring(simpleName.lastIndexOf('/') + 1);

            localOutDir = new File(localOutDir, packageName);
        }

        String fileName = simpleName + ".class";
        File targetFile = new File(localOutDir, fileName);

        if (targetFile.exists()) {
            try {
                // System.out.println("Loading dumped lambda from: " + targetFile.getAbsolutePath());
                byte[] diskBytes = Files.readAllBytes(targetFile.toPath());

                return diskBytes;
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.err.println("Dumped lambda not found: " + targetFile.getAbsolutePath());
            return c;
        }

        System.err.println("error");
        return null;
    }

}
