import java.util.Vector;

public final class breaksPast {
	public static void main(String[] args) {
		Vector v = new Vector();
		int i = 0;
		while (!v.isEmpty() && i < args[0].length()) {
			char c = args[0].charAt(i);
			++i;
			if (c != 'i') {
				v.add(c);
				System.out.println(v.size());
			}
			else {
				Object d = v.get(0);
				System.out.println(v.size());
				v.remove(0);
				System.out.println(d);
			}
		}
	}
}