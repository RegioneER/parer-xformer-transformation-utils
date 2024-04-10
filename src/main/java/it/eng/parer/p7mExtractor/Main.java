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
import java.io.File;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.FilenameUtils;

public class Main {

    public static void main(String[] args) throws Throwable {

        // File folder = new File("/tmp/output/extracted");
        String output_dir = "/home/cek/tmp/final";
        // File[] listOfFiles = folder.listFiles();
        //
        // for (File file : listOfFiles) {
        // if (file.isFile() && file.getName().endsWith(".p7m")) {
        // P7mExtractor ex = new P7mExtractor();
        // boolean risultato = ex.elaboraV3(file.getAbsolutePath(), output_dir + "/" + file.getName());
        // //String risultato = ex.elabora(file, output_dir + "/" +
        // FilenameUtils.getBaseName(file.getAbsolutePath()),"/tmp/"+
        // FilenameUtils.getBaseName(file.getAbsolutePath()));
        // System.out.println("ecco: " + risultato + " " + file.getName());
        //
        //
        // }
        // }

        P7mExtractor ex = new P7mExtractor();
        //
        //// String[] files = new File("/tmp/output/extracted/").list();
        //// for (String file : files) {
        //// ex.elaboraV2("/tmp/output/extracted/" + file, "/tmp/test" + file + ".xml");
        //// }
        //
        boolean risultato = ex.elaboraV3(
                "/home/cek/Work/parer/235160116.urn_unimoney27-unimaticaspa-it_umcomuanzola.MANDATO.1.137965834.XML_PKCS7_PKCS7",
                "/tmp/test.xml");
        // boolean risultato = ex.elaboraV3("/tmp/test.xml", "/tmp/test2.xml");
        // boolean risultato =
        // ex.elaboraV3("/tmp/output/extracted/comunecamugnano_OIL-bancaunicredit_2017_2017/INTERREV/INTERREVERSALI_2017_04_20_08_47_43/INTERREV_1/INTERREV_1.p7m",
        // "/tmp/test.xml");
        System.out.println("ecco: " + risultato);

        // Files.walk(Paths.get(new URI("file:///tmp/output/extracted")))
        // .filter(Files::isRegularFile)
        // .forEach(path -> {
        // System.out.println(path);
        // if (path.toString().endsWith(".p7m")) {
        // P7mExtractor ex = new P7mExtractor();
        // try {
        // boolean risultato = ex.elaboraV3(path.toString(), output_dir + "/" + path.getFileName().toString());
        // System.out.println("ecco: " + risultato);
        // } catch (ExtractorException ex1) {
        // Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex1);
        // }
        // }
        // });
    }
}
