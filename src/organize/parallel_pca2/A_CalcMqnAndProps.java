package organize.parallel_pca2;

import chemaxon.marvin.calculations.*;
import chemaxon.struc.MolAtom;
import chemaxon.struc.MolBond;
import chemaxon.struc.Molecule;
import chemaxon.util.MolHandler;
import java.io.FileWriter;
import java.io.IOException;
import organize.tools.SMIReadWriter;

/**
 * Start of parallel PCA procedure, start reading here Calculates fingerprint
 * (MQN) & map properties (Ringatoms, PSA) of SMILES input and writes it to
 * output file. The code mostly copied from basic MQN calculator. Second the
 * sums of each MQN value is calculated and stored at the end in a .sums file.
 * This is then used vor mean centralizsation in the next step B/C.
 */
public class A_CalcMqnAndProps {

    public static final int fplength = 42;
    static HBDAPlugin hbdap = new HBDAPlugin();
    static MajorMicrospeciesPlugin mmp = new MajorMicrospeciesPlugin();
    static TopologyAnalyserPlugin tap = new TopologyAnalyserPlugin();
    static ElementalAnalyserPlugin eap = new ElementalAnalyserPlugin();
    static logPPlugin lpp = new logPPlugin();
    static TPSAPlugin psap = new TPSAPlugin();
    static long[] sums = new long[fplength];
    static long okMolCount = 0;

    public static void main(String[] args) throws IOException {
        SMIReadWriter smio = new SMIReadWriter(
                "usage: -i smiles[.gz] -o mqns[.gz] -s sums.dat [-m]\n"
                + "-i input file with smiles (with space separated tag)\n"
                + "-o output file with smiles + mqn(;) + csaproperties(;)\n"
                + "-s output file with sum of mqn values(;) + no molecules\n"
                + "[-m calculates major species at pH7.4]\n"
                + "dehydrogenize & dearomatize is done by default", args);

        System.out.println("IN: " + smio.getArg("-i") + " OUT: " + smio.getArg("-o")
                + " SUMS: " + smio.getArg("-s") + " MMS: " + smio.isArg("-m"));

        System.out.println("READING IN, CALCULATING MQNS AND PROPERTIES, SUMMING MQNS AND WRITING OUT. HARM WITH VALENCE");

        String res;
        String s;
        //read input file and annotate with mqns and properties
        while ((s = smio.readLine()) != null) {
            smio.displayReadCounter(1000);
            String[] sarr = s.split("\t");

            //handle tag
            String tag = "";
            sarr[1]=sarr[1].replace(";", "_");
            if (sarr.length > 1) {
                tag = ";" + sarr[1];
            }                        

            Molecule m;
            try {
                m = new MolHandler(sarr[0]).getMolecule();
            } catch (Exception e) {
                continue;
            }

            //if -m is set take major species @pH7.4 if fails skip it
            //this is one of my favourite java-coding-lines. smart, no?
            if (smio.isArg("-m") && (m = majorSpecies(m)) == null) {
                continue;
            }

            //dehydro & dearom molecule, if it fails skip it
            if ((m = harmonize(m)) == null) {
                continue;
            }

            //Cap the hac to 60
            if (m.getAtomCount() > 50) {
                continue;
            }

            String qsmi;
            try {
                qsmi = m.toFormat("smiles:q");
            } catch (Exception e) {
                continue;
            }


            //calculate values for that thing
            if ((res = calculateFingerprint(m)) == null) {
                continue;
            }

            //write unique pH adjusted smiles (+ eventually tag) + resulting MQN + map data
            smio.writeLine(qsmi + tag + " " + res);
        }
        smio.end();

        //write out the sums of the mqn values plus the number of processed molecules
        //for calculating the average in the next step B_MergeSums
        System.out.println("WRITING SUMS");
        FileWriter fw = new FileWriter(smio.getArg("-s"));
        for (int i = 0; i < sums.length; i++) {
            fw.write(sums[i] + ";");
        }
        fw.write(" " + okMolCount + "\n");
        fw.close();
        System.out.println("END");
    }

    /*
     * Do a valence check, dehydrogenize and dearomatize mol
     */
    public static Molecule harmonize(Molecule m) {

        try {
            m.valenceCheck();
            if (m.hasValenceError()) {
                System.err.println("VALENCE ERROR " + m.toFormat("smiles"));
                return null;
            }

            m.hydrogenize(false);
            if (!m.dearomatize()) {
                System.err.println("DEAROMATIZE ERROR " + m.toFormat("smiles"));
                return null;
            }

        } catch (Exception e) {
            System.out.println("ERROR DURING HARMONIZATION");
            return null;
        }
        return m;
    }

    /*
     * to pH 7.4
     */
    public static Molecule majorSpecies(Molecule m) {
        try {
            mmp.setMolecule(m);
            mmp.setpH(7.4);
            mmp.run();
            m = mmp.getMajorMicrospecies();
        } catch (Exception e) {
            e.toString();
            System.err.println("MMP ERROR " + m.toFormat("smiles"));
            return null;
        }
        return m;
    }

    /*
     * This code is a mixture of the calcMQN method from the CMC original paper
     * plus some more lines for the ASF map properties (logP TPSA rigatoms...)
     */
    public static String calculateFingerprint(Molecule m) {
        //initialize everything
        try {
            hbdap.setMolecule(m);
            hbdap.run();
            tap.setMolecule(m);
            tap.run();
            eap.setMolecule(m);
            eap.run();
            lpp.setMolecule(m);
            lpp.run();
            psap.setMolecule(m);
            psap.run();
        } catch (Exception e) {
            e.toString();
            System.err.println("CalcPlugin Error " + m.toFormat("smiles"));
            return null;
        }

        /*
         * THIS CODE IS MOSTLY COPIED FROM MQN PAPER
         */
        //Classic descriptors
        int hbd = hbdap.getDonorAtomCount();
        int hbdm = hbdap.getDonorCount();
        int hba = hbdap.getAcceptorAtomCount();
        int hbam = hbdap.getAcceptorCount();
        int rbc = tap.getRotatableBondCount();

        //Ring properties / ring sizes count
        int r3 = 0, r4 = 0, r5 = 0, r6 = 0, r7 = 0, r8 = 0, r9 = 0, rg10 = 0;
        int[][] sssr = m.getSSSR();
        for (int i = 0; i < sssr.length; i++) {
            switch (sssr[i].length) {
                case 3:
                    r3++;
                    break;
                case 4:
                    r4++;
                    break;
                case 5:
                    r5++;
                    break;
                case 6:
                    r6++;
                    break;
                case 7:
                    r7++;
                    break;
                case 8:
                    r8++;
                    break;
                case 9:
                    r9++;
                    break;
                default:
                    rg10++;
                    break;
            }
        }

        //Atom properties
        int c = 0, f = 0, cl = 0, br = 0, I = 0, thac = 0, asv = 0, adv = 0, atv = 0, aqv = 0,
                cdv = 0, ctv = 0, cqv = 0, p = 0, s = 0, posc = 0, negc = 0,
                afrc = 0, cn = 0, an = 0, co = 0, ao = 0;
        int ringat = 0;
        for (int i = 0; i < m.getAtomCount(); i++) {
            MolAtom at = m.getAtom(i);
            boolean isRingAt = tap.isRingAtom(i);
            if (isRingAt) {
                ringat++;
            }
            if (at.getAtno() != 1) {
                thac++;
            }
            //element counts
            switch (at.getAtno()) {
                case 1:
                case 2:
                case 3:
                case 4:
                case 5:
                    break;
                case 6:
                    c++;
                    break;
                case 7:
                    if (isRingAt) {
                        cn++;
                    } else {
                        an++;
                    }
                    break;
                case 8:
                    if (isRingAt) {
                        co++;
                    } else {
                        ao++;
                    }
                    break;
                case 15:
                    p++;
                    break;
                case 16:
                    s++;
                    break;

                case 9:
                    f++;
                    break;
                case 17:
                    cl++;
                    break;
                case 35:
                    br++;
                    break;
                case 53:
                    I++;
                    break;
            }

            //valency count
            switch (at.getBondCount()) {
                case 0:
                    System.err.println("ATOM WITH NO BONDS ");
                    return null;
                case 1:
                    asv++; //single valent can only be acyclic
                    break;
                case 2:
                    if (isRingAt) {
                        cdv++;
                    } else {
                        adv++;
                    }
                    break;
                case 3:
                    if (isRingAt) {
                        ctv++;
                    } else {
                        atv++;
                    }
                    break;
                case 4:
                    if (isRingAt) {
                        cqv++;
                    } else {
                        aqv++;
                    }
                    break;
            }
            if (tap.getRingCountOfAtom(i) > 1) {
                afrc++;
            }
        }

        //Bond properties
        int csb = 0, cdb = 0, ctb = 0, asb = 0, adb = 0, atb = 0, bfrc = 0;
        for (int i = 0; i < m.getBondCount(); i++) {
            MolBond bd = m.getBond(i);
            if (tap.isRingBond(i)) {
                switch (bd.getType()) {
                    case 1:
                        csb++;
                        break;
                    case 2:
                        cdb++;
                        break;
                    case 3:
                        ctb++;
                        break;
                    default:
                        System.err.println("UNKNOWN CYCLIC BOND TYPE " + bd.getType());
                        return null;
                }
            } else {
                switch (bd.getType()) {
                    case 1:
                        asb++;
                        break;
                    case 2:
                        adb++;
                        break;
                    case 3:
                        atb++;
                        break;
                    default:
                        System.err.println("UNKNOWN ACYCLIC BOND TYPE " + bd.getType());
                        return null;
                }
            }
        }

        //bond's fused ring count
        int[][] sssre = m.getSSSRBonds();
        int[] brc = new int[m.getBondCount()];
        for (int j = 0; j < sssre.length; j++) {
            for (int k = 0; k < sssre[j].length; k++) {
                brc[sssre[j][k]]++;
            }
        }
        for (int j = 0; j < brc.length; j++) {
            if (brc[j] > 1) { //if bond's ring count > 1
                bfrc++; //increase fused ring bonds count
            }
        }

        for (int i = 0; i < m.getAtomCount(); i++) {
            int crg = m.getAtom(i).getCharge();
            if (crg > 0) {
                posc += crg;
            }
            if (crg < 0) {
                negc += Math.abs(crg);
            }
        }

        //additional values for CSA map
        double mass = eap.getMass();
        double psa = psap.getSurfaceArea();
        double logP = lpp.getlogPTrue();
        int ringc = sssr.length;


        /*
         * END OF COPY PARTY
         */
        //now before return put all mqns into sum array (will be used for value-average calcualtion
        //in next step B_MergeSumsToAvg)
        okMolCount++;
        sums[0] += hbd;
        sums[1] += hbdm;
        sums[2] += hba;
        sums[3] += hbam;
        sums[4] += rbc;
        sums[5] += r3;
        sums[6] += r4;
        sums[7] += r5;
        sums[8] += r6;
        sums[9] += r7;
        sums[10] += r8;
        sums[11] += r9;
        sums[12] += rg10;
        sums[13] += thac;
        sums[14] += c;
        sums[15] += p;
        sums[16] += s;
        sums[17] += f;
        sums[18] += cl;
        sums[19] += br;
        sums[20] += I;
        sums[21] += cn;
        sums[22] += an;
        sums[23] += co;
        sums[24] += ao;
        sums[25] += asv;
        sums[26] += adv;
        sums[27] += atv;
        sums[28] += aqv;
        sums[29] += cdv;
        sums[30] += ctv;
        sums[31] += cqv;
        sums[32] += afrc;
        sums[33] += posc;
        sums[34] += negc;
        sums[35] += csb;
        sums[36] += cdb;
        sums[37] += ctb;
        sums[38] += asb;
        sums[39] += adb;
        sums[40] += atb;
        sums[41] += bfrc;

        return //first block is MQNs
                //CLASSIC PROPERTIES
                hbd + ";" //hydrogen bond donor atom count 1
                + hbdm + ";" //HBD with multivalency 2
                + hba + ";" //hydrogen bond acceptor atom count 3
                + hbam + ";" //HBA with multivalency 4
                + rbc + ";" //rotatable bond count 5
                //RING PROPERTIES Counts 6-13
                + r3 + ";" + r4 + ";" + r5 + ";" + r6 + ";" + r7 + ";" + r8 + ";" + r9 + ";" + rg10 + ";" //ATOM PROPERTIES
                + thac + ";"//total heavy atom count (= everything else than H D T) 14
                + c + ";" //carbon count 15
                + p + ";"//phosphorus 16
                + s + ";"//sulfur atom count 17
                + f + ";" //fluor atom count 18
                + cl + ";" //chlorine atom count 19
                + br + ";" //bromine atom count 20
                + I + ";" //iodine atom count 21
                + cn + ";" //cyclic nitrogen count 22
                + an + ";" //acyclic nitrogen count 23
                + co + ";" //cyclic oxygen count 24
                + ao + ";" //acyclic oxygen count 25
                + asv + ";"//acyclic single valent atom count 26
                + adv + ";"//acyclic double valent atom count 27
                + atv + ";"//acyclic triple valent atom count 28
                + aqv + ";"//acyclic quart valent atom count 29
                + cdv + ";"//cyclic double valent atom count 30
                + ctv + ";"//cyclic triple valent atom count 31
                + cqv + ";"//cyclic quart valent atom count 32
                + afrc + ";"//atoms-in-fused-ring count 33
                + posc + ";" // Positive charges 34
                + negc + ";" // Negative charges 35
                //BOND PROPERTIES
                + csb + ";"//cyclic single bonds 36
                + cdb + ";"//cyclic double bonds 37
                + ctb + ";"//cyclic triple bonds 38
                + asb + ";"//acyclic singe bonds 39
                + adb + ";"//acyclic double bonds 40
                + atb + ";"//acyclic triple bonds 41
                + bfrc + " "//bonds-in-fused-ring count 42
                //last block is csa properties WARNING IF YOU MODIFIY HERE YOU HAVE TO MODIFY G_CREATEPARTMAPS
                //String[] names = {"hac","rbc","ringc","hbd","hba","logp","psa","carb","ringat"};
                + thac + ";"
                + rbc + ";"
                + ringc + ";"
                + hbd + ";"
                + hba + ";"
                + logP + ";"
                + psa + ";"
                + c + ";"
                + ringat + ";";
    }
}
