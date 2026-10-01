import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;

/**
 * Runtime proof of the bigint_patch mechanism.
 *
 * bigint_patch rewrites java.math.BigInteger.oddModPow so that whenever the modulus
 * equals PortSwigger's hardcoded official RSA modulus, it is silently replaced by the
 * keygen's own modulus. That is what makes a keygen-signed license verify.
 *
 * Run this WITHOUT the agent to see the true result, then WITH the agent to see that
 * "base ^ e mod N_official" now secretly returns "base ^ e mod N_attacker".
 */
public class RtTest {
    public static void main(String[] args) throws Exception {
        BigInteger nOff = new BigInteger(Files.readString(Path.of("/tmp/official_N.txt")).trim());
        BigInteger nAtt = new BigInteger(Files.readString(Path.of("/tmp/attacker_N.txt")).trim());
        BigInteger e    = BigInteger.valueOf(65537);
        BigInteger base = new BigInteger("1234567890123456789012345678901234567890");

        // Reference results computed with each modulus. modPow on an odd modulus routes to oddModPow.
        BigInteger rOff = base.modPow(e, nOff);
        BigInteger rAtt = base.modPow(e, nAtt);

        System.out.println("official_N (first 40): " + nOff.toString().substring(0, 40) + "...");
        System.out.println("attacker_N (first 40): " + nAtt.toString().substring(0, 40) + "...");
        System.out.println();
        System.out.println("base.modPow(e, N_official) -> " + (rOff.equals(rAtt) ? "== attacker result" : "== official result"));
        System.out.println("  result(first 40): " + rOff.toString().substring(0, 40) + "...");
        System.out.println();
        if (rOff.equals(rAtt)) {
            System.out.println(">>> bigint_patch IS ACTIVE: modPow with the official modulus was");
            System.out.println(">>> silently redirected to the keygen modulus. License-signature");
            System.out.println(">>> verification against the official key is subverted. CRACK LIVE.");
        } else {
            System.out.println(">>> bigint_patch did NOT fire (results differ): either no agent, or");
            System.out.println(">>> BigInteger was already loaded before premain (no retransform).");
        }

        // Agent-independent: confirm the keygen's private key modulus == attacker_N (internal consistency).
        if (args.length > 0 && args[0].equals("--check-key")) {
            byte[] der = hexToBytes(Files.readString(Path.of("/tmp/pk2048hex.txt")).trim());
            KeyFactory kf = KeyFactory.getInstance("RSA");
            RSAPrivateKey pk = (RSAPrivateKey) kf.generatePrivate(new PKCS8EncodedKeySpec(der));
            boolean matches = pk.getModulus().equals(nAtt);
            System.out.println();
            System.out.println("[key-consistency] privateKey2048 modulus == attacker_N (bigint_patch target): " + matches);
        }
    }

    static byte[] hexToBytes(String s) {
        int n = s.length();
        byte[] out = new byte[n / 2];
        for (int i = 0; i < n; i += 2)
            out[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
        return out;
    }
}
