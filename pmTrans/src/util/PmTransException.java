package util;

public class PmTransException extends Exception {

	private static final long serialVersionUID = -5799812327900993309L;

	public PmTransException(String msg, Throwable cause) {
		super(msg, cause);
	}

	public PmTransException(String msg) {
		super(msg);
	}

}
