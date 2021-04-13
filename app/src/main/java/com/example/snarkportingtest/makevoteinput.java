package com.example.snarkportingtest;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Scanner;



public class makevoteinput extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        String param = (String) intent.getExtras().get("param");
        int start = 0, end = 0;
        FileReader fileReader = null;
//        FileWriter fwTmp = null;
        PrintWriter fwTmp = null;
        File filetmp = null;
        int counter = 0;
        int mode = 0;
        if(param.equals("PP")) { start = 1; end = 3; mode = 0;}
        else if(param.equals("e_id")){ start = 4; end = 11; mode = 1;}
        else if(param.equals("ek_id")) { start = 12; end = 13; mode = 0;}
        else if(param.equals("dir_sel")) { start = 14; end = 14; mode = 1;}
        else if(param.equals("candidate")) { start = 15; end = 268; mode = 1;}
        else if(param.equals("sk_id")) { start = 269; end = 276; mode = 1;}
        else if(param.equals("rand_enc")) { start = 277; end = 277; mode = 1;}
        else if(param.equals("hashwires")) { start = 278; end = 325; mode = 1;}

        else if(param.equals("enc1x")) { start = 326; end = 328; mode = 1;}//mod r, mod q, basepoint.y
        else if(param.equals("ec1x d")) { start = 329; end = 582; mode = 1;} //double 254
        else if(param.equals("ec1x a")) { start = 583; end = 836; mode = 1;} //add 254
        else if(param.equals("ec1x s")) { start = 837; end = 837; mode = 1;}// sub 1개

        else if(param.equals("enc1y")) { start = 838; end = 840; mode = 1;} //mod r, mod q, basepoint.y
        else if(param.equals("ec1y d")) { start = 841; end = 1094; mode = 1;} //double 254
        else if(param.equals("ec1y a")) { start = 1095; end = 1348; mode = 1;} //add 254
        else if(param.equals("ec1y s")) { start = 1349; end = 1349; mode = 1;}// sub 1개

        else if(param.equals("op1")){ start = 1350; end = 1352; mode = 1;}//operation base.y, h.y
//        else if(param.equals("op1 a")){ start = 1352; end = 1352; mode = 1;}//add 1개

        else if(param.equals("enc2x")){ start = 1353; end = 1355; mode = 1;}//mod r, mod q, basepoint.y
        else if(param.equals("ec2x d")) { start = 1356; end = 1609; mode = 1;} //double 254
        else if(param.equals("ec2x a")) { start = 1610; end = 1863; mode = 1;} //add 254
        else if(param.equals("ec2x s")) { start = 1864; end = 1864; mode = 1;}// sub 1개

        else if(param.equals("enc2y")) { start = 1865; end = 1867; mode = 1;} //mod r, mod q, basepoint.y
        else if(param.equals("ec2y d")) { start = 1868; end = 2121; mode = 1;} //double 254
        else if(param.equals("ec2y a")) { start = 2122; end = 2375; mode = 1;} //add 254
        else if(param.equals("ec2y s")) { start = 2376; end = 2376; mode = 1;}// sub 1개

        else if(param.equals("op2")){ start = 2377; end = 2379; mode = 1;}//operation base, h
//        else if(param.equals("op2 a")){ start = 2379; end = 2379; mode = 1;}//add 1개


        //1350 base 1351 h 1352 divison result
        /// 1353~1354 mod // 1355 group y //  1356~1864 ec lambda // 509개
        // 1865~1866 mod  // 1867 group y //  1868~2379 ec lambda // 512개


        BufferedReader scanner = null;
        String inFilePath = "/data/data/com.example.snarkportingtest/files/votein";
        if(mode == 0) {
            Log.d("makevoteinput", "mode 0 write " + param);
            String line = null;
            try {

                CopyIfNotExist(R.raw.votein, inFilePath + ".txt");
                fileReader = new FileReader(new File(inFilePath + ".txt"));
                filetmp = new File(inFilePath + "tmp.txt");
                fwTmp = new PrintWriter(filetmp);
                scanner = new BufferedReader(fileReader);
                while ((line = scanner.readLine()) != null) {
                    counter++;
                    if (counter >= start && counter <= end) {
//                        Log.d("readline : ", line);
                        fwTmp.println(line);
                    }
                    else{
                        fwTmp.println(line);
                    }
                }
            } catch (FileNotFoundException e) {
                System.err.println(e);
            } catch (IOException e) {
                System.err.println(e);
            }
            finally {
                fwTmp.close();
            }
        }
        else if(mode == 1) {
            Log.d("makevoteinput", "mode 1 write " + param);
            String line = null;
            String[] values = (String[]) intent.getExtras().get("values");

            try {
                fileReader = new FileReader(new File(inFilePath + ".txt"));
                filetmp = new File(inFilePath + "tmp.txt");
                fwTmp = new PrintWriter(filetmp);
                scanner = new BufferedReader(fileReader);
                while ((line = scanner.readLine()) != null) {
                    counter++;
                    if (counter >= start && counter <= end) {
//                        Log.d("readline : ", line.split(" ")[0]);
                        fwTmp.printf("%s %s\n", line.split(" ")[0], values[counter-start]);
                    }
                    else{
                        fwTmp.println(line);
                    }
                }
            } catch (FileNotFoundException e) {
                System.err.println(e);
            } catch (IOException e) {
                System.err.println(e);
            } catch (Exception e){
                Log.d("makeinput error", e.toString());
            }
            finally {
                fwTmp.close();
            }
        }

        else {
            Log.d("no pp", "sibal");
        }
        filetmp.renameTo(new File(inFilePath+".txt"));
        finish();
    }


    public BigInteger[] split(BigInteger x, int chunksize) {
		int numChunks = (int)Math.ceil(x.bitLength()*1.0/chunksize);
		BigInteger[] chunks = new BigInteger[numChunks];
		BigInteger mask = new BigInteger("2").pow(chunksize).subtract(BigInteger.ONE);
		for (int i = 0; i < numChunks; i++) {
			chunks[i] = x.shiftRight(chunksize * i).and(mask);
		}
		return chunks;
	}

    public void CopyFromPackage(int resID, String target) throws IOException
    {
        FileOutputStream lOutputStream = openFileOutput(target, Context.MODE_PRIVATE);
        InputStream lInputStream = getResources().openRawResource(resID);
        int readByte;
        byte[] buff = new byte[999999];

        while (( readByte = lInputStream.read(buff))!=-1)
        {
            lOutputStream.write(buff,0, readByte);
        }

        lOutputStream.flush();
        lOutputStream.close();
        lInputStream.close();
    }
    public void CopyIfNotExist(int resID, String target) throws IOException
    {
        File targetFile = new File(target);
        //if (!targetFile.exists())
        //{
            CopyFromPackage(resID,targetFile.getName());
        //}
    }
}
