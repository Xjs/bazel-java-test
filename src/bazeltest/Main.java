package bazeltest;

import java.nio.ByteBuffer;

public class Main {
	public static void main(String[] args) {
		ByteBuffer bb = ByteBuffer.allocate(132);
		bb.put((byte) 'a');
	}
}