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

import it.eng.parer.p7mExtractor.*;

public class Main {

    public static void main(String[] args) throws Throwable {

        // File folder = new File("/tmp/output/extracted");
        // String output_dir = "/home/cek/tmp/final";
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
        TSDExtractor tdse = new TSDExtractor();
        boolean risultato = tdse.elabora(
                "/home/cek/Work/parer/IdC-PDA-201708220000001587_1503364022593_2311.xml.p7m.tsd",
                "/home/cek/Work/parer/UDs", "tsd_file");

        // String[] files = new File("/tmp/output/extracted/").list();
        // for (String file : files) {
        // ex.elaboraV2("/tmp/output/extracted/" + file, "/tmp/test" + file + ".xml");
        // }
        // boolean risultato =
        // ex.elaboraV3("/home/cek/Work/parer/unicredit/2012/INTERREV/INTERREVERSALI_2012_12_06_09_37_36_33/INTERREV_1/INTERREV_1.p7m",
        // "/tmp/test.xml");
        System.out.println("ecco: " + risultato);
    }
}
