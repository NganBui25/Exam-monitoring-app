package src.Client;

import src.Client.view.Home;
import org.opencv.core.Core;

public class Main{

	public static void main(String[] args) {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		new Home().setVisible(true);
	}
}