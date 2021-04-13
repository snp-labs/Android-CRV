package makeinputs;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.math.BigInteger;
import java.util.Scanner;

import android.util.Log;

public class voteInput{
    public voteInput(int msgSize){
        try{
            String inFilePath = new String("/data/data/com.example.snarkportingtest/files/votein");
            Scanner scanner = new Scanner(new File(inFilePath+".txt"));
            File filetmp = new File(inFilePath+".tmp");
			File filePublic = new File("./votePP.in");
            File filePrivate = new File("./vote.in");
            
            FileWriter fwTmp = new FileWriter(filetmp,true);
			FileReader frPublic = new FileReader(filePublic);
            FileReader frPrivate = new FileReader(filePrivate);
            
			BufferedReader brPublic = new BufferedReader(frPublic);
            BufferedReader brPrivate = new BufferedReader(frPrivate);
            
            fwTmp.write(scanner.next() +" "+ scanner.next() + "\n");
            for(int i = 0; i < msgSize;i++){ //e_id 2~9
                fwTmp.write(scanner.next() +" "+ scanner.next() + "\n");
            }
            for(int i = 0; i < msgSize;i++){ //pp[0] = G 10~17
                fwTmp.write(scanner.next() +" "+ scanner.next() + "\n");
            }
            for(int i = 0; i < msgSize;i++){ //EK_id[0] = S 18~25
                fwTmp.write(scanner.next() +" "+ scanner.next() + "\n");
            }
            for(int i = 0; i < msgSize;i++){ //pp[1] = Grho 26~33
                fwTmp.write(scanner.next() +" "+ scanner.next() + "\n");
            }
            for(int i = 0; i < msgSize;i++){ //EK_id[1] = T 34~41
                fwTmp.write(scanner.next() +" "+ scanner.next() + "\n");
            }
            BigInteger dirselector = new BigInteger(brPrivate.readLine());
            // fwTmp.write();  //directionselector 42
            // fwTmp.write();  //msg 43
            BigInteger sk_id = new BigInteger(brPrivate.readLine());
            BigInteger[] skid = split(sk_id, 32); 
            for(int i = 0; i < skid.length;i++){  //44~51
                fwTmp.write(scanner.next() +" "+ skid[i].toString(16) + "\n"); scanner.next();
            }
            String s = scanner.next() +" "+  dirselector.toString(16) + "\n";
            fwTmp.write(s);
            s = scanner.next();
            while(scanner.hasNext()){
                BigInteger path = new BigInteger(brPrivate.readLine());
                s = scanner.next() +" "+  path.toString(16) + "\n";
                fwTmp.write(s);
                s = scanner.next();
            }
            //14064 ~ 14111 -> intermediateHashWires
			fwTmp.close();
			brPublic.close();
            brPrivate.close();
            
            Runtime runtime = Runtime.getRuntime();
            Process process = runtime.exec("mv ./voting_tmp.in " + inFilePath);
            System.out.println(inFilePath);
		}catch(FileNotFoundException e){
			System.err.println(e);
		}catch(IOException e){
			System.err.println(e);
		}
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

	public voteInput(String param, int position){
        // 원본파일경로
        String fileName = "/data/data/com.example.snarkportingtest/files/votein";

        File file = new File(fileName+".txt");

        String dummy = "";
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            //BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)));

            //1. 삭제하고자 하는 position 이전까지는 이동하며 dummy에 저장

            String line;

            for(int i=0; i<position; i++) {

                line = br.readLine(); //읽으며 이동

                dummy += (line + "\r\n" );

            }
            //2. 삭제하고자 하는 데이터는 건너뛰기

            String delData = br.readLine();
            Log.d("mstag","삭제되는 데이터 = "+delData);

            //3. 삭제하고자 하는 position 이후부터 dummy에 저장
            while((line = br.readLine())!=null) {
                dummy += (line + "\r\n" );
            }

            //4. FileWriter를 이용해서 덮어쓰기
            FileWriter fw = new FileWriter(fileName+".txt");
            fw.write(dummy);
            //bw.close();
            fw.close();
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    public static void main(String[] args) {
        System.out.println(args.length);
        voteInput mInput = new voteInput(8);
    }
}
