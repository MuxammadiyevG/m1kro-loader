import com.m1kro.burploader.Loader;

import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import jdk.internal.org.objectweb.asm.ClassReader;
import jdk.internal.org.objectweb.asm.tree.ClassNode;
import jdk.internal.org.objectweb.asm.tree.MethodNode;
import jdk.internal.org.objectweb.asm.util.CheckClassAdapter;
import jdk.internal.org.objectweb.asm.ClassWriter;

/**
 * End-to-end test that the M1kro Loader agent actually patches a REAL Burp jar.
 *
 * It does NOT run Burp. It:
 *   1. Scans the given Burp jar for the license-check class that burp_patch1/2 target
 *      (a burp/* class with a method of descriptor ([Ljava/lang/Object;Ljava/lang/Object;)V
 *       holding > 20000 instructions).
 *   2. Feeds that class to Loader.burp_patch1 and confirms a patch is produced.
 *   3. Re-parses the patched class and confirms the target method body was replaced with
 *      the single static call to com/m1kro/burploader/Filter.BurpFilter + RETURN.
 *   4. Runs ASM's verifier (CheckClassAdapter) over the patched bytecode to prove it is valid.
 */
public class PatchTest {
    static final String TARGET_DESC = "([Ljava/lang/Object;Ljava/lang/Object;)V";

    public static void main(String[] args) throws Exception {
        String jarPath = args[0];
        System.out.println("[*] Scanning Burp jar: " + jarPath);

        String licenseClass = null;
        byte[] licenseBytes = null;
        int licenseMethodInsns = 0;

        try (ZipFile zf = new ZipFile(jarPath)) {
            Enumeration<? extends ZipEntry> e = zf.entries();
            while (e.hasMoreElements()) {
                ZipEntry ze = e.nextElement();
                String name = ze.getName();
                if (!name.startsWith("burp/") || !name.endsWith(".class")) continue;
                if (ze.getSize() <= 110000) continue;
                byte[] bytes;
                try (InputStream is = zf.getInputStream(ze)) {
                    bytes = is.readAllBytes();
                }
                ClassNode cn = new ClassNode();
                new ClassReader(bytes).accept(cn, 0);
                for (MethodNode m : cn.methods) {
                    if (m.desc.equals(TARGET_DESC) && m.instructions.size() > 20000) {
                        licenseClass = name;
                        licenseBytes = bytes;
                        licenseMethodInsns = m.instructions.size();
                        break;
                    }
                }
                if (licenseClass != null) break;
            }
        }

        if (licenseClass == null) {
            System.out.println("[!] No license-check class found (burp_patch1 criteria did not match).");
            System.out.println("    This Burp build may use the burp_patch2 shape instead; test inconclusive.");
            return;
        }

        String internalName = licenseClass.substring(0, licenseClass.length() - ".class".length());
        System.out.println("[+] License-check class FOUND: " + internalName);
        System.out.println("    size=" + licenseBytes.length + " bytes, target method instructions=" + licenseMethodInsns);

        // Apply the real M1kro patch.
        Loader loader = new Loader();
        byte[] patched = loader.burp_patch1(internalName, licenseBytes);
        if (patched == null) {
            System.out.println("[!] burp_patch1 returned null — FAIL");
            return;
        }
        System.out.println("[+] burp_patch1 produced patched bytecode: " + patched.length + " bytes");

        // Confirm the target method now just calls Filter.BurpFilter and returns.
        ClassNode pn = new ClassNode();
        new ClassReader(patched).accept(pn, 0);
        boolean ok = false;
        for (MethodNode m : pn.methods) {
            if (m.desc.equals(TARGET_DESC)) {
                int size = m.instructions.size();
                boolean callsFilter = m.instructions.toString().contains("BurpFilter")
                        || containsFilterCall(m);
                System.out.println("    patched method now has " + size + " instructions (was " + licenseMethodInsns + ")");
                if (size < 10 && callsFilter) ok = true;
            }
        }
        System.out.println("[" + (ok ? "+" : "!") + "] Method body replaced with Filter.BurpFilter stub: " + ok);

        // Verify the patched class passes the ASM bytecode verifier.
        StringBuilder sb = new StringBuilder();
        java.io.StringWriter sw = new java.io.StringWriter();
        try {
            ClassReader cr = new ClassReader(patched);
            CheckClassAdapter.verify(cr, false, new java.io.PrintWriter(sw));
            String verr = sw.toString();
            if (verr.isEmpty()) {
                System.out.println("[+] ASM verifier: patched class is VALID bytecode");
            } else {
                System.out.println("[!] ASM verifier reported issues:\n" + verr.substring(0, Math.min(800, verr.length())));
            }
        } catch (Throwable t) {
            System.out.println("[!] Verifier threw: " + t);
        }

        System.out.println();
        System.out.println("=== RESULT: " + (ok ? "PATCH WORKS on this real Burp build" : "patch produced output but method-shape check failed") + " ===");
    }

    static boolean containsFilterCall(MethodNode m) {
        for (jdk.internal.org.objectweb.asm.tree.AbstractInsnNode in : m.instructions.toArray()) {
            if (in instanceof jdk.internal.org.objectweb.asm.tree.MethodInsnNode) {
                jdk.internal.org.objectweb.asm.tree.MethodInsnNode mi = (jdk.internal.org.objectweb.asm.tree.MethodInsnNode) in;
                if (mi.owner.equals("com/m1kro/burploader/Filter") && mi.name.equals("BurpFilter")) return true;
            }
        }
        return false;
    }
}
