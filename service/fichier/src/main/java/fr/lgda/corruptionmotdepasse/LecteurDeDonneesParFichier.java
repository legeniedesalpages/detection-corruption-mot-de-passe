package fr.lgda.corruptionmotdepasse;

import fr.lgda.corruptionmotdepasse.service.LecteurDonnees;
import name.neuhalfen.projects.crypto.bouncycastle.openpgp.BouncyGPG;
import org.apache.commons.io.IOUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openpgp.*;
import org.bouncycastle.openpgp.jcajce.JcaPGPObjectFactory;
import org.bouncycastle.openpgp.operator.jcajce.JcaKeyFingerprintCalculator;
import org.bouncycastle.openpgp.operator.jcajce.JcaPGPContentVerifierBuilderProvider;
import org.bouncycastle.openpgp.operator.jcajce.JcePBESecretKeyDecryptorBuilder;
import org.bouncycastle.openpgp.operator.jcajce.JcePublicKeyDataDecryptorFactoryBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.URI;
import java.nio.charset.Charset;
import java.security.Security;
import java.util.Arrays;
import java.util.Iterator;
import java.util.logging.Logger;
import java.util.stream.Stream;

@Component
public class LecteurDeDonneesParFichier implements LecteurDonnees {

    private static final Logger LOG = Logger.getLogger(LecteurDeDonneesParFichier.class.getName());

    PGPSecretKeyRingCollection pgpSec;
    private JcePublicKeyDataDecryptorFactoryBuilder factoryBuilder;

    private JcaPGPContentVerifierBuilderProvider jcaPGPContentVerifierBuilderProvider;

    private PGPPublicKey publicKey;

    JcePBESecretKeyDecryptorBuilder secretKey;

    @Value("${app.gpg-key}")
    private String keyPath;

    @Value("${app.gpg-public}")
    private String publicPath;

    @Override
    public Stream<String> lireDonnees(URI uri) {

        try {
            pgpSec = secretKeyRingCollection(keyPath);
            factoryBuilder = new JcePublicKeyDataDecryptorFactoryBuilder().setProvider("BC");
            jcaPGPContentVerifierBuilderProvider = new JcaPGPContentVerifierBuilderProvider().setProvider("BC");
            publicKey = publicKeyGenerator();
            secretKey = secretKeyDecryptorBuilder();
            //Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
            InputStream in = new FileInputStream(new File(uri));


            OutputStream out = decrypt(in, "TechTeamNumber1");
            // Lire les données décryptées
            return IOUtils.readLines(new BufferedInputStream(new ByteArrayInputStream(((ByteArrayOutputStream) out).toByteArray())), Charset.defaultCharset()).stream();
        } catch (Exception e) {
            throw new RuntimeException("Impossible de lire le fichier:" + uri, e);
        }

    }

    public OutputStream decrypt(InputStream in, String password) throws IOException {
        in = PGPUtil.getDecoderStream(in);
        InputStream clearDataStream = null;
        InputStream compressedDataStream = null;
        PGPLiteralData literalData = null;
        OutputStream out = new ByteArrayOutputStream();

        try {
            JcaPGPObjectFactory pgpF = new JcaPGPObjectFactory(in);
            PGPEncryptedDataList enc;

            // Le premier objet est peut-être un marqueur PGP
            if (pgpF.nextObject() instanceof PGPEncryptedDataList pgpEncryptedDataList) {
                enc = pgpEncryptedDataList;
            } else {
                enc = (PGPEncryptedDataList) pgpF.nextObject();
            }

            // On cherche la clé privée
            Iterator<PGPEncryptedData> it = enc.getEncryptedDataObjects();
            PGPPrivateKey sKey = null;
            PGPPublicKeyEncryptedData pbe = null;

            while (sKey == null && it.hasNext()) {
                pbe = (PGPPublicKeyEncryptedData) it.next();
                sKey = findSecretKey(pgpSec, pbe.getKeyID(), password.toCharArray());
            }

            if (sKey == null) {
                throw new RuntimeException("Aucune clé privée trouvée pour le message");
            }

            clearDataStream = pbe.getDataStream(factoryBuilder.build(sKey));

            JcaPGPObjectFactory plainFact = new JcaPGPObjectFactory(clearDataStream);

            Object message = plainFact.nextObject();

            JcaPGPObjectFactory pgpFact = null;
            boolean isCompressed = false;
            if (message instanceof PGPCompressedData cData) {
                compressedDataStream = cData.getDataStream();
                pgpFact = new JcaPGPObjectFactory(compressedDataStream);
                message = pgpFact.nextObject();
                isCompressed = true;
            }

            boolean isVerified = false;
            if (message instanceof PGPOnePassSignatureList p1) {
                PGPOnePassSignature ops = p1.get(0);
                if (isCompressed) {
                    literalData = (PGPLiteralData) pgpFact.nextObject();
                } else {
                    literalData = (PGPLiteralData) plainFact.nextObject();
                }

                ops.init(jcaPGPContentVerifierBuilderProvider, publicKey);
                InputStream dIn = literalData.getInputStream();
                int ch;
                while ((ch = dIn.read()) >= 0) {
                    ops.update((byte) ch);
                    out.write(ch);
                }
                out.close();

                PGPSignatureList signatureList;
                if (isCompressed) {
                    signatureList = (PGPSignatureList) pgpFact.nextObject();
                } else {
                    signatureList = (PGPSignatureList) plainFact.nextObject();
                }

                isVerified = ops.verify(signatureList.get(0));
            }

            if (isVerified) {
                IOUtils.copyLarge(literalData.getInputStream(), out);
            } else if (message instanceof PGPLiteralData pgpLiteralData) {
                IOUtils.copyLarge(pgpLiteralData.getInputStream(), out);
            } else {
                throw new RuntimeException("Le message n'est pas simplement chiffré, le type est inconnu");
            }
        } catch (PGPException e) {
            throw new RuntimeException("Erreur lors du déchiffrement du fichier", e);
        } finally {
            if(clearDataStream != null) clearDataStream.close();
            if(compressedDataStream != null) compressedDataStream.close();
            if(literalData != null && literalData.getInputStream() != null) literalData.getInputStream().close();
        }

        return out;
    }


    public JcePBESecretKeyDecryptorBuilder secretKeyDecryptorBuilder() {
        // A ajouter sinon le provider "BC" n'est pas reconnu !
        if (Arrays.stream(Security.getProviders()).noneMatch(BouncyCastleProvider.class::isInstance)) {
            Security.addProvider(new BouncyCastleProvider());
        }
        return new JcePBESecretKeyDecryptorBuilder().setProvider("BC");
    }

    private PGPPrivateKey findSecretKey(PGPSecretKeyRingCollection pgpSec, long keyID, char[] pass) throws PGPException {
        PGPSecretKey pgpSecKey = pgpSec.getSecretKey(keyID);
        return pgpSecKey == null ? null : pgpSecKey.extractPrivateKey(secretKey.build(pass));
    }

    public PGPSecretKeyRingCollection secretKeyRingCollection(String secretKeyResource) throws Exception {
        try (InputStream inputStream = new FileInputStream(new File(new URI(secretKeyResource)));
             InputStream stream = PGPUtil.getDecoderStream(inputStream)) {
            return new PGPSecretKeyRingCollection(stream, new JcaKeyFingerprintCalculator());
        }
    }


    public PGPPublicKeyRingCollection publicKeyRingCollection(String publicResourceKey) throws Exception {
        try (InputStream inputStream = new FileInputStream(new File(new URI(publicResourceKey))); InputStream stream = PGPUtil.getDecoderStream(inputStream)) {
            return new PGPPublicKeyRingCollection(stream, new JcaKeyFingerprintCalculator());
        }
    }

    public PGPPublicKey publicKeyGenerator() throws Exception {
        PGPPublicKeyRingCollection pgpPub = publicKeyRingCollection(publicPath);
        Iterator<PGPPublicKeyRing> keyRingIter = pgpPub.getKeyRings();
        while (keyRingIter.hasNext()) {
            PGPPublicKeyRing keyRing = keyRingIter.next();

            Iterator<PGPPublicKey> keyIter = keyRing.getPublicKeys();
            while (keyIter.hasNext()) {
                PGPPublicKey key = keyIter.next();

                if (key.isEncryptionKey()) {
                    return key;
                }
            }
        }

        throw new Exception("Can't find encryption key in key ring.");
    }

}
