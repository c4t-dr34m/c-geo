package carnero.cgeo;

import android.text.Spannable;
import java.util.ArrayList;
import java.util.Date;

public class cgTrackable {
	public int errorRetrieve = 0;
	public String error = "";
	public String guid = "";
	public String geocode = "";
	public String name = "";
	public String nameString = null;
	public Spannable nameSp = null;
	public String type = null;
	public Date released = null;
	public String origin = null;
	public String owner = null;
	public String spottedName = null;
	public String spottedGuid = null;
	public String goal = null;
	public String details = null;
	public String image = null;
	public ArrayList<cgLog> logs = new ArrayList<cgLog>();
}
