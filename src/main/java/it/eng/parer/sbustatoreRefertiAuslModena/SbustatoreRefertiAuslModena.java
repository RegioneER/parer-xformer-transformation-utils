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

package it.eng.parer.sbustatoreRefertiAuslModena;

import it.eng.parer.ExtractorException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import javax.mail.BodyPart;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.CMSSignedData;

public class SbustatoreRefertiAuslModena {

    public List<String> elabora(String fileConPath, String folderOut) throws Exception {

        File file = new File(fileConPath);

        File directoryPathFilesExtractedFile = new File(folderOut);
        String directoryPathFilesExtracted = directoryPathFilesExtractedFile.getAbsolutePath();
        if (!directoryPathFilesExtractedFile.exists()) {
            if (!directoryPathFilesExtractedFile.mkdirs()) {
                throw new Exception("Impossibile creare la directory '" + directoryPathFilesExtractedFile + "'.");
            }
        }

        boolean extractionResult = extractP7m(file.getAbsolutePath(),
                directoryPathFilesExtracted + File.separator + "Estratto.xml"); // FIXME Questo fallisce sempre? Fprse
                                                                                // si può togliere

        if (!extractionResult) {
            // * 2) è un base64 - p7m - multipart */
            extractionResult = extractBase64(file.getAbsolutePath(), directoryPathFilesExtracted, "Estratto.xml");
        }

        List<String> filenames = extractMultipart(directoryPathFilesExtracted + File.separator + "Estratto.xml",
                directoryPathFilesExtracted);

        // boolean result = extractBase64(file.getAbsolutePath(), directoryPathFilesExtracted, "Estratto.xml");
        //
        // String generatedFilename = directoryPathFilesExtracted + File.separator + "Estratto.xml";
        // if (!result)
        // generatedFilename = file.getAbsolutePath();
        //
        // List<String> filenames = extractMultipart(directoryPathFilesExtracted + File.separator + "Estratto.xml",
        // directoryPathFilesExtracted);

        return filenames;
    }

    private boolean extractP7m(String p7mFile, String extraxctP7mFile) throws ExtractorException {
        FileOutputStream fileOutputStream = null;

        File file = new File(p7mFile);
        try {
            byte[] readFileToByteArray = readFileToByteArray(file);

            if (readFileToByteArray.length <= 0)
                throw new ExtractorException("File " + p7mFile + " ha dimensione zero.");

            CMSSignedData csd = new CMSSignedData(readFileToByteArray);
            CMSProcessableByteArray cpb = (CMSProcessableByteArray) csd.getSignedContent();
            byte[] signedContent = (byte[]) cpb.getContent();
            fileOutputStream = new FileOutputStream(extraxctP7mFile);
            fileOutputStream.write(signedContent);

        } catch (IOException | CMSException ex) {
            return false;
        } finally {
            if (fileOutputStream != null)
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    throw new ExtractorException("Errore in chiusura estrazione p7m : " + e.getMessage());
                }
        }

        return true;
    }

    private boolean extractBase64(String p7mFile, String extraxctP7mDirectory, String filename)
            throws ExtractorException {
        FileOutputStream fos = null;
        try {
            String encoded = readFile(p7mFile);
            fos = new FileOutputStream(extraxctP7mDirectory + File.separator + "Estratto.xml.b64");
            org.bouncycastle.util.encoders.Base64.decode(encoded, fos);
        } catch (Exception ex) {
            return false;
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    throw new ExtractorException("Errore in chiusura extractBase64: " + e.getMessage());
                }
            }
        }

        try {
            boolean extractionResult = extractP7m(extraxctP7mDirectory + File.separator + "Estratto.xml.b64",
                    extraxctP7mDirectory + File.separator + filename);
            return extractionResult;
        } catch (Exception e) {
            throw new ExtractorException("Errore in estrazione p7m: " + e.getMessage());
        } finally {
            new File(extraxctP7mDirectory + File.separator + "Estratto.xml.b64").delete();
        }
    }

    private static byte[] readFileToByteArray(File file) throws IOException {
        byte[] b;
        try (InputStream is = new FileInputStream(file)) {
            long length = file.length();
            b = new byte[(int) length];
            is.read(b);
        }
        return b;
    }

    private static String readFile(String fileName) throws IOException {
        BufferedReader br = null;
        String ret = "";
        try {
            String sCurrentLine;
            br = new BufferedReader(new FileReader(fileName));
            while ((sCurrentLine = br.readLine()) != null) {
                ret += sCurrentLine + "\n";
            }
        } catch (IOException e) {
            throw e;
        } finally {
            try {
                if (br != null)
                    br.close();
            } catch (IOException ex) {
                throw ex;
            }
        }
        return ret;
    }

    private List<String> extractMultipart(String fileMultipart, String directoryPathFilesExtracted) throws Exception {
        FileInputStream fis = null;
        List<String> filenames = new ArrayList<String>();

        try {
            fis = new FileInputStream(fileMultipart);

            ByteArrayDataSource ds = new ByteArrayDataSource(fis, "multipart/mixed");
            MimeMultipart multipart = new MimeMultipart(ds);

            for (int j = 0; j < multipart.getCount(); j++) {
                BodyPart bodyPart = multipart.getBodyPart(j);
                String filename = bodyPart.getFileName();

                if (filename == null || filename.isEmpty()) {
                    filename = "non_dichiarato-" + j + ".xml";
                }

                filenames.add(filename);

                savefile(directoryPathFilesExtracted, filename, bodyPart.getInputStream());
            }

        } finally {
            if (fis != null) {
                fis.close();
            }
        }

        return filenames;
    }

    private File savefile(String directoryPath, String FileName, InputStream is) throws IOException {
        File f = new File(directoryPath, FileName);
        FileOutputStream fos = new FileOutputStream(f);
        byte[] buf = new byte[4096];
        int bytesRead;
        while ((bytesRead = is.read(buf)) != -1) {
            fos.write(buf, 0, bytesRead);
        }
        fos.close();
        return f;
    }
}
