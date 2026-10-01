import java.io.InputStream;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import jdk.internal.org.objectweb.asm.ClassReader;
import jdk.internal.org.objectweb.asm.tree.ClassNode;
import jdk.internal.org.objectweb.asm.tree.MethodNode;

/** Diagnostic: understand the license-check class shape in a given Burp jar. */
public class Diag {
    public static void main(String[] args) throws Exception {
        String jar = args[0];
        System.out.println("[*] " + jar);
        int bigClasses = 0;
        List<String> objObjMethods = new ArrayList<>();      // methods with desc ([LObject;[LObject;...)
        String[] biggestClass = {null}; int[] biggestLen = {0};
        int[] maxInsnAny = {0}; String[] maxInsnWhere = {null};

        try (ZipFile zf = new ZipFile(jar)) {
            Enumeration<? extends ZipEntry> e = zf.entries();
            while (e.hasMoreElements()) {
                ZipEntry ze = e.nextElement();
                String name = ze.getName();
                if (!name.startsWith("burp/") || !name.endsWith(".class")) continue;
                byte[] bytes;
                try (InputStream is = zf.getInputStream(ze)) { bytes = is.readAllBytes(); }
                if (bytes.length > biggestLen[0]) { biggestLen[0] = bytes.length; biggestClass[0] = name; }
                if (bytes.length > 110000) bigClasses++;
                ClassNode cn = new ClassNode();
                try { new ClassReader(bytes).accept(cn, 0); } catch (Throwable t) { continue; }
                for (MethodNode m : cn.methods) {
                    int sz = m.instructions.size();
                    if (sz > maxInsnAny[0]) { maxInsnAny[0] = sz; maxInsnWhere[0] = name + " " + m.name + m.desc; }
                    if (m.desc.equals("([Ljava/lang/Object;Ljava/lang/Object;)V")) {
                        objObjMethods.add(name + " " + m.name + " insns=" + sz + " (classBytes=" + bytes.length + ")");
                    }
                }
            }
        }
        System.out.println("    burp/* classes > 110KB: " + bigClasses);
        System.out.println("    biggest burp class: " + biggestClass[0] + " (" + biggestLen[0] + " bytes)");
        System.out.println("    largest single method anywhere: " + maxInsnAny[0] + " insns @ " + maxInsnWhere[0]);
        System.out.println("    methods with desc ([Ljava/lang/Object;Ljava/lang/Object;)V : " + objObjMethods.size());
        for (String s : objObjMethods) System.out.println("        " + s);
    }
}
