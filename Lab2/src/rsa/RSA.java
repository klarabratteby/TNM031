package rsa;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Random;



public class RSA {

public BigInteger p, q, n,v,e,d,c,m, message;

public static String input;
private String de_message;

public void calculate() {
	
	// Step 1 n = p*q
	// bitlength 1024 (max 2048)
	p = BigInteger.probablePrime(1024, new SecureRandom());
	q = BigInteger.probablePrime(1024, new SecureRandom());
	n = p.multiply(q);
	
	// Step 2, encryption exponent, gcd(e,(p-1)(q-1)) =1
	v = p.subtract(BigInteger.ONE).multiply(q.subtract(BigInteger.ONE));
	
	// generate e, odd number
	e = generate(v);
	
	// Step 3, decryption exponent
	d = e.modInverse(v); // private key, mod(v)
	
	// ENCRYPTION
	message = new BigInteger(input.getBytes());
	c = message.modPow(e,n); // public key, m^e
	
	// DECRYPTION (using d)
	//decrypted message
	m = c.modPow(d,n); //c^d
	
	// String 
	de_message = new String(m.toByteArray());
};

private BigInteger generate(BigInteger v) {
	  BigInteger e1;
      Random rand = new Random();

      do {
          // Generate a random number 
          e1 = new BigInteger(v.bitLength(), rand);
      // while e1 is bigger or equal to v, continue to generate numbers
      // while the greatest common divisor is not equal to one, continue to generate numbers
      } while (e1.compareTo(v) >= 0 || !e1.gcd(v).equals(BigInteger.ONE));

      return e1;

}

public static void main(String[] arg) throws IOException {
	System.out.println("Enter message: ");
	
	input = (new BufferedReader(new InputStreamReader(System.in))).readLine();
	
	RSA i = new RSA();
	i.calculate();
	
	System.out.println("e: " + i.e);
	System.out.println("d: " + i.d);
	System.out.println("c: "+ i.c);
	System.out.println("Decrypted message: " + i.de_message);
		
}

}
