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

package it.eng.parer.tsdExtractor;

import it.eng.parer.ExtractorException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1OutputStream;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DEREncodable;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERTaggedObject;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.cms.CMSSignedData;

public class TSDExtractor {

    public boolean elabora(String tdsFileName, String outputDirectory, String fileNamePrefix)
            throws ExtractorException {
        boolean isTDS = false;

        File tdsFile = new File(tdsFileName);

        File directoryPathFilesExtractedFile = new File(outputDirectory);
        if (!directoryPathFilesExtractedFile.exists()) {
            if (!directoryPathFilesExtractedFile.mkdirs()) {
                throw new ExtractorException(
                        "Impossibile creare la directory '" + directoryPathFilesExtractedFile + "'.");
            }
        }

        try (FileInputStream fis = new FileInputStream(tdsFile); ASN1InputStream ais = new ASN1InputStream(fis)) {
            ASN1Sequence sequence = ASN1Sequence.getInstance(ais.readObject());
            for (int i = 0; i < sequence.size(); i++) {
                DEREncodable element = sequence.getObjectAt(i);

                if (element instanceof ASN1ObjectIdentifier) {
                    ASN1ObjectIdentifier identifier = (ASN1ObjectIdentifier) element;
                    if (identifier.equals(PKCSObjectIdentifiers.id_ct_timestampedData)) {
                        isTDS = true;
                    }
                } else if (element instanceof DERTaggedObject) {
                    DERTaggedObject taggedObject = (DERTaggedObject) element;
                    ASN1Sequence timeStampedData = DERSequence.getInstance(taggedObject.getObject());

                    for (int n = 0; n < timeStampedData.size(); n++) {
                        if (timeStampedData.getObjectAt(n) instanceof DEROctetString) {
                            DEROctetString octetString = (DEROctetString) timeStampedData.getObjectAt(n);

                            CMSSignedData cmssd = new CMSSignedData(octetString.getOctetStream());

                            File p7mFile = new File(outputDirectory, fileNamePrefix + ".p7m");
                            try (FileOutputStream fos = new FileOutputStream(p7mFile)) {
                                fos.write(octetString.getOctets());
                            }

                            File pdfFile = new File(outputDirectory, fileNamePrefix + ".xml");
                            try (FileOutputStream fos = new FileOutputStream(pdfFile)) {
                                cmssd.getSignedContent().write(fos);
                            }

                        } else if (timeStampedData.getObjectAt(n) instanceof DERTaggedObject) {
                            taggedObject = (DERTaggedObject) timeStampedData.getObjectAt(n);
                            if (isTDS) {
                                ASN1Sequence internalSequence = DERSequence.getInstance(taggedObject.getObject());
                                for (int j = 0; j < internalSequence.size(); j++) {
                                    if (internalSequence.getObjectAt(j) instanceof DERSequence) {
                                        File tsrFile = new File(outputDirectory, fileNamePrefix + ".tsr");

                                        try (FileOutputStream fos = new FileOutputStream(tsrFile);
                                                ASN1OutputStream aos = new ASN1OutputStream(fos)) {
                                            aos.writeObject(internalSequence.getObjectAt(j));
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

            }

        } catch (Exception ex) {
            throw new ExtractorException("Errore in estrazione tsd : " + ex.getMessage());
        }

        return true;
    }
}
