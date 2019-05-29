package bazeltest;

import java.nio.ByteBuffer;
import java.io.IOException;

public class Main {
	public static void main(String[] args) {
		try {
			ByteBufferWriter.write(ByteBuffer.allocate(42), System.out);
		} catch (IOException e) {
			System.out.println("IOException: " + e.getMessage());
		}
		System.out.println(BazelTest.compare(42));
	}
}