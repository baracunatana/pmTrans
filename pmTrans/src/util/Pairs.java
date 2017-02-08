package util;

import java.util.List;
import java.util.Vector;

public class Pairs {

	List<Object[]> list;

	public Pairs() {
		list = new Vector<Object[]>();
	}

	public String getValue(Integer k) {
		for (Object[] o : list) {
			if (o[0].equals(k))
				return (String) o[1];
		}
		return null;
	}

	public Integer getKey(String v) {
		for (Object[] o : list) {
			if (((String) o[1]).equalsIgnoreCase((String) v))
				return (Integer) o[0];
		}
		return null;
	}

	public boolean add(Integer k, String v) {
		if (getKey(v) != null || getValue(k) != null)
			return false;
		Object[] obs = { k, v };
		list.add(obs);
		return true;
	}

}
