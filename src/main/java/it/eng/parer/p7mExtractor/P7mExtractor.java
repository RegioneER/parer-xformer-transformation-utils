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

package it.eng.parer.p7mExtractor;

import it.eng.parer.ExtractorException;
import org.apache.tika.Tika;

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
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;

import org.apache.commons.io.FilenameUtils;
import org.apache.tika.config.TikaConfig;
import org.apache.tika.detect.Detector;
import org.apache.tika.detect.EncodingDetector;
import org.apache.tika.exception.TikaException;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.CMSSignedData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class P7mExtractor {

    private static Logger logger;
    private static Tika tika = new Tika();

    public boolean elaboraV3(String fileConPath, String fileOut) throws ExtractorException {
        logger = LoggerFactory.getLogger(P7mExtractor.class);
        logger.info("[P7mExtractor2] Importazione file " + fileConPath);

        // separa il nome del file dal path
        String fileNameOut = new File(fileOut).getName();
        String dirOut = fileOut.substring(0, fileOut.length() - fileNameOut.length());

        File file = new File(fileConPath);

        File directoryPathFilesExtractedFile = new File(dirOut);
        String directoryPathFilesExtracted = directoryPathFilesExtractedFile.getAbsolutePath();
        if (!directoryPathFilesExtractedFile.exists()) {
            if (!directoryPathFilesExtractedFile.mkdirs()) {
                throw new ExtractorException(
                        "Impossibile creare la directory '" + directoryPathFilesExtractedFile + "'.");
            }
        }

        TikaConfig tikaConfig;
        Detector tikaDetector;
        EncodingDetector tikaEncodingDetector;

        try {
            tikaConfig = new TikaConfig();
            tikaDetector = tikaConfig.getDetector();
            tikaEncodingDetector = tikaConfig.getEncodingDetector();
        } catch (TikaException | IOException ex) {
            throw new ExtractorException("Impossibile inizializzare TIKA: '" + ex.getMessage() + "'.");
        }

        // questo ciclo serve a sbustare i file imbustati più volte (MAX 3 volte).
        boolean done = false;
        int count = 0;
        while (!done && count < 3) {
            /* 1) file p7m */
            boolean extractionResult = extractP7m(file.getAbsolutePath(), fileOut);

            if (!extractionResult) {
                // * 2) è un base64 - p7m - multipart */
                extractionResult = extractBase64(file.getAbsolutePath(), directoryPathFilesExtracted, fileNameOut);
            }

            if (!extractionResult) {
                extractionResult = extractPKCS7(file.getAbsolutePath(), fileOut);
            }

            MediaType mediaType;
            try (InputStream is = new FileInputStream(new File(fileOut))) {
                TikaInputStream stream = TikaInputStream.get(is);

                Metadata metadata = new Metadata();
                metadata.add(Metadata.RESOURCE_NAME_KEY, fileNameOut);
                mediaType = tikaDetector.detect(stream, metadata);
            } catch (IOException ex) {
                throw new ExtractorException("Errore TIKA: '" + ex.getMessage() + "'.");
            }

            // se ho fallito l'estrazione o il file è già sbustato del tutto o non sono in grado di sbustarlo.
            if (!extractionResult) {
                done = true;
            }

            // MEV #23449: questi mime type devono essere sbustati più volte.
            if (!mediaType.toString().equals("application/pkcs7-signature")
                    && !mediaType.toString().equals("application/x-dbf")) {
                done = true;
            }

            file = new File(fileOut);
            count++;
        }

        return done;
    }

    private boolean extractP7m(String p7mFile, String extraxctP7mFile) throws ExtractorException {
        FileOutputStream fileOutputStream = null;

        File file = new File(p7mFile);
        try {
            byte[] readFileToByteArray = readFileToByteArray(file);

            if (readFileToByteArray.length <= 0) {
                throw new ExtractorException("File " + p7mFile + " ha dimensione zero.");
            }

            CMSSignedData csd = new CMSSignedData(readFileToByteArray);
            CMSProcessableByteArray cpb = (CMSProcessableByteArray) csd.getSignedContent();
            byte[] signedContent = (byte[]) cpb.getContent();
            fileOutputStream = new FileOutputStream(extraxctP7mFile);
            fileOutputStream.write(signedContent);

        } catch (IOException | CMSException ex) {
            return false;
        } finally {
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    throw new ExtractorException("Errore in chiusura estrazione p7m : " + e.getMessage());
                }
            }
        }

        return true;
    }

    private boolean extractPKCS7(String p7mFile, String extraxctP7mFile) throws ExtractorException {
        FileOutputStream fileOutputStream = null;
        BufferedReader reader = null;

        File file = new File(p7mFile);
        try {
            String PEM_PKCS7_HEADER = "-----BEGIN PKCS7-----";
            String PEM_PKCS7_FOOTER = "-----END PKCS7-----";

            reader = new BufferedReader(new FileReader(file));
            StringBuilder cerfile = new StringBuilder();
            String signedContent = null;
            while ((signedContent = reader.readLine()) != null) {
                if (!signedContent.contains(PEM_PKCS7_HEADER) && !signedContent.contains(PEM_PKCS7_FOOTER)) {
                    cerfile.append(signedContent);
                }
            }

            fileOutputStream = new FileOutputStream(extraxctP7mFile);
            byte[] data = org.bouncycastle.util.encoders.Base64.decode(cerfile.toString());

            CMSSignedData csd = new CMSSignedData(data);
            CMSProcessableByteArray cpb = (CMSProcessableByteArray) csd.getSignedContent();
            byte[] content = (byte[]) cpb.getContent();
            fileOutputStream.write(content);

        } catch (Exception ex) {
            return false;
        } finally {
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    throw new ExtractorException("Errore in chiusura estrazione p7m : " + e.getMessage());
                }
            }

            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    throw new ExtractorException("Errore in chiusura estrazione p7m : " + e.getMessage());
                }
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
                    throw new ExtractorException("Errore in chiusura extractBase64 " + e.getMessage());
                }
            }
        }

        try {
            boolean extractionResult = extractP7m(extraxctP7mDirectory + File.separator + "Estratto.xml.b64",
                    extraxctP7mDirectory + File.separator + filename);
            return extractionResult;
        } finally {
            new File(extraxctP7mDirectory + File.separator + "Estratto.xml.b64").delete();
        }
    }

    // private boolean extractMimeMultipart(String p7mFile, String extraxctP7mDirectory, String filename) {
    // try {
    // FileDataSource ds = new FileDataSource(p7mFile);
    // MimeMultipart message = new MimeMultipart(ds);
    // for (int i = 0; i < message.getCount(); i++) {
    // BodyPart bodyPart = message.getBodyPart(i);
    // if (bodyPart.isMimeType("application/pkcs7-mime")) {
    // ByteArrayInputStream content = (ByteArrayInputStream) bodyPart.getContent();
    // IOUtils.copy(content, new FileOutputStream(extraxctP7mDirectory + File.separator + filename + "_" + i));
    // }
    //
    // }
    //
    //
    // } catch (Exception ex) {
    // return false;
    // }
    //
    // return true;
    // }
    /**
     *
     * @param directoryPath
     * @param FileName
     * 
     * @return Se nella directory passata (directoryPath) non e' presente il file fileName restituisce fileName
     *         altrimenti lancia eccezione
     * 
     * @throws IOException
     */
    private static String checkUniqueFileName(String directoryPath, String fileName) throws ExtractorException {
        File file = new File(FilenameUtils.concat(directoryPath, fileName));
        if (file.exists()) {
            throw new ExtractorException("Impossibile salvare il file '" + fileName + "' nella dorectory '"
                    + directoryPath + "' perche' un file con lo stesso nome e' gia' presente.");
        }
        return fileName;
    }

    private static File savefile(String directoryPath, String FileName, InputStream is) throws IOException {
        File f = new File(FilenameUtils.concat(directoryPath, FileName));
        try (FileOutputStream fos = new FileOutputStream(f)) {
            byte[] buf = new byte[4096];
            int bytesRead;
            while ((bytesRead = is.read(buf)) != -1) {
                fos.write(buf, 0, bytesRead);
            }
        }
        return f;
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
                if (br != null) {
                    br.close();
                }
            } catch (IOException ex) {
                throw ex;
            }
        }
        return ret;
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

    private static List<File> extractMultipart(String fileMultipart, String directoryPathFilesExtracted,
            String defaultFileName) throws ExtractorException, IOException {
        List<File> filesExtracted = new ArrayList<>();
        try (FileInputStream fis = new FileInputStream(fileMultipart)) {
            ByteArrayDataSource ds = new ByteArrayDataSource(fis, "multipart/mixed");
            MimeMultipart multipart = new MimeMultipart(ds);
            filesExtracted = parseMessage(directoryPathFilesExtracted, multipart, defaultFileName);

        } catch (Exception ex) {
            throw new ExtractorException("Impossibile estrarre i files multipart");
        }
        return filesExtracted;
    }

    private static List<File> parseMessage(String directoryPath, Multipart multipart, String defaultFileName)
            throws MessagingException, IOException, ExtractorException {
        List<File> filesExtracted = new ArrayList<>();
        for (int j = 0; j < multipart.getCount(); j++) {
            BodyPart bodyPart = multipart.getBodyPart(j);

            if (!Part.ATTACHMENT.equalsIgnoreCase(bodyPart.getDisposition())) {
                if (bodyPart.getContent().getClass().equals(MimeMultipart.class)) {
                    MimeMultipart mimemultipart = (MimeMultipart) bodyPart.getContent();
                    for (int k = 0; k < mimemultipart.getCount(); k++) {
                        if (mimemultipart.getBodyPart(k).getFileName() != null) {
                            filesExtracted.add(savefile(directoryPath,
                                    checkUniqueFileName(directoryPath, mimemultipart.getBodyPart(k).getFileName()),
                                    mimemultipart.getBodyPart(k).getInputStream()));
                        }
                    }
                } else if (bodyPart.isMimeType("text/html")) {
                    if (bodyPart.getFileName() != null) {
                        filesExtracted.add(savefile(directoryPath,
                                checkUniqueFileName(directoryPath, bodyPart.getFileName()), bodyPart.getInputStream()));
                    } else {
                        String fileNameXml = checkUniqueFileName(directoryPath, defaultFileName + ".xml");
                        filesExtracted.add(savefile(directoryPath, fileNameXml, bodyPart.getInputStream()));
                    }
                }
                continue;
            }
            filesExtracted.add(savefile(directoryPath, checkUniqueFileName(directoryPath, bodyPart.getFileName()),
                    bodyPart.getInputStream()));
        }

        return filesExtracted;
    }
}
