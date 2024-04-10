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

import java.io.File;
import java.util.Collection;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author Cappelli_F
 */
public class Main {

    public static void main(String[] args) throws Throwable {
        if (args.length != 4) {
            System.out.println("Utilizzo: ordinatividecript <iso_filename> <tmp_dir> <final_dir> <password>");
            System.exit(0);
        }

        OrdinativiDecriptor od = new OrdinativiDecriptor();

        File iso = new File(args[0]);
        File tempExtractionDir = new File(args[1]);
        File destDir = new File(args[2]);
        String password = args[3]; // "cJqvSRMPkZhbMhhaLAFf13jtUXC4XQ+x4mK4YFbpocDJ/Fc0tqJ9ATwcbKB7bByx/hMYQsjujcczCzRKiYtqxA";

        String[] extensions = { "krt", "KRT" };

        try {
            System.out.println("Inizio decrittazione di " + iso.getCanonicalPath());

            if (!destDir.exists()) {
                destDir.mkdirs();
            }

            // System.out.print("Estrazione ISO... ");
            // od.extractIso(iso, tempExtractionDir);
            // System.out.println("OK");

            Collection<File> encryptedFiles = FileUtils.listFiles(tempExtractionDir, extensions, true);

            for (File encryptedFile : encryptedFiles) {
                String originalDirectory = encryptedFile.getParent().replace(tempExtractionDir.getAbsolutePath(), "");
                String newFilename = encryptedFile.getName().substring(0, encryptedFile.getName().lastIndexOf("."));
                File newPath = new File(destDir + File.separator + originalDirectory + File.separator + newFilename);

                newPath.getParentFile().mkdirs();

                System.out.print(encryptedFile.getAbsolutePath() + " --> " + newPath);
                od.decrypt(encryptedFile, newPath, password);
                System.out.println(" ... OK");
            }
            System.out.println("Fine decrittazione di " + iso.getCanonicalPath());
        } finally {
            FileUtils.deleteDirectory(tempExtractionDir);
        }
    }
}
