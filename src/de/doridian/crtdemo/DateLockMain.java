package de.doridian.crtdemo;

import javax.swing.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateLockMain {
	public static void main(String[] args) throws Exception {
		Date minimal = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").parse("2014/12/25 00:00:00");
		Date now = new Date();

		if(now.before(minimal)) {
			JOptionPane.showMessageDialog(null, "Do not open your gift early ;P");
		} else {
			CRTDemoMain.main(args);
		}
	}
}
