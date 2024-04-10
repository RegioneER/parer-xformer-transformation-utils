/*
 * Engineering Ingegneria Informatica S.p.A.
 *
 * Copyright (C) 2023 Regione Emilia-Romagna
 * <p/>
 * This program is free software: you can redistribute it and/or modify it under the terms of
 * the GNU Affero General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 * <p/>
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <https://www.gnu.org/licenses/>.
 */

package it.eng.parer.ordinativiDecriptor;

import com.github.stephenc.javaisotools.loopfs.iso9660.Iso9660FileEntry;
import com.github.stephenc.javaisotools.loopfs.iso9660.Iso9660FileSystem;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import javax.crypto.Cipher;
import org.apache.commons.io.FileUtils;
import org.encryptor4j.util.FileEncryptor;

/**
 *
 * @author Cappelli_F
 */
public class OrdinativiDecriptor {
    private Iso9660FileSystem discFs;

    public void decrypt(File encryptedFilePath, File destFile, String password) throws Exception {
        decodeFile(password, encryptedFilePath, destFile);
    }

    public void decodeFile(String pin, File srcFile, File dstFile) throws Exception {
        PublicKey publicKey = getPublicKey();
        String aesKey = decodeKey(pin, publicKey);
        FileEncryptor fe = new FileEncryptor(aesKey);
        fe.decrypt(srcFile, dstFile);
    }

    private PublicKey getPublicKey() throws Exception {
        URL res = getClass().getClassLoader().getResource("pubKey.data");
        byte[] data = FileUtils.readFileToByteArray(new File(res.toURI()));
        byte[] publicKeyByte = Base64.getDecoder().decode(data);
        X509EncodedKeySpec x509Spec = new X509EncodedKeySpec(publicKeyByte);
        KeyFactory kfPublic = KeyFactory.getInstance("RSA");
        PublicKey publicKey = kfPublic.generatePublic(x509Spec);
        return publicKey;
    }

    private String decodeKey(String aesCrypetKey, PublicKey publicKey) throws Exception {
        Cipher decryptCipher = Cipher.getInstance("RSA");
        decryptCipher.init(2, publicKey);
        byte[] aesDecodedKey = decryptCipher.doFinal(Base64.getDecoder().decode(aesCrypetKey));
        return new String(aesDecodedKey);
    }

    public void extractIso(File isoToRead, File saveLocation) throws Exception {
        try {
            // Give the file and mention if this is treated as a read only file.
            discFs = new Iso9660FileSystem(isoToRead, true);
        } catch (IOException e) {
            throw new Exception(e);
        }

        // Make our saving folder if it does not exist
        if (!saveLocation.exists()) {
            saveLocation.mkdirs();
        }

        // Go through each file on the disc and save it.
        for (Iso9660FileEntry singleFile : discFs) {
            if (singleFile.isDirectory()) {
                new File(saveLocation, singleFile.getPath()).mkdirs();
            } else {
                File tempFile = new File(saveLocation, singleFile.getPath());
                try {
                    Files.copy(discFs.getInputStream(singleFile), tempFile.toPath());
                } catch (IOException e) {
                    throw new Exception(e);
                }
            }
        }
    }
}
