import org.junit.jupiter.api.Test;

import java.util.Random;

public class TestGenerator {
	static class Term {
		Object a;
		char op;
		Object b;

		public Term(Object a, char op, Object b) {
			this.a = a;
			this.op = op;
			this.b = b;
		}
		
		@Override
		public String toString() {
			return "(" + a + " " + op + " (" + b + "))";
		}
	}
	
	public static Object gen(Random rnd, boolean java, int depth) {
		if(depth == 3) {
			return rnd.nextBoolean() ? rnd.nextInt(1000) + (java ? ".0F" : "") : (java ? "a" : "query.a");
		}
		
		return new Term(gen(rnd, java, depth + 1), "+-*/".charAt(rnd.nextInt(4)), gen(rnd, java, depth + 1));
	}

	@Test
	public void main() {
		for(int i = 0; i < 16; i++) {
			String expr = gen(new Random(i), false, 0).toString();
			System.out.println("test(\"" + expr + "\", " + gen(new Random(i), true, 0) + ");");
		}
	}
}