import br.com.etyllica.EtyllicaApplet;
import br.com.etyllica.core.context.Application;
import br.com.etyllica.util.PathHelper;
import br.com.nrobot.MainMenu;


public class NinjaRobotApplet extends EtyllicaApplet {

	private static final long serialVersionUID = 787422233224705125L;

	public NinjaRobotApplet() {
		super(800, 600);
	}

	@Override
	public Application startApplication() {
		
		String s = PathHelper.currentDirectory();
		setPath(s+"../");
		
		return new MainMenu(w, h);
	}

}
