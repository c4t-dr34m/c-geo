package carnero.cgeo;

import java.util.ArrayList;
import org.xml.sax.AttributeList;
import org.xml.sax.HandlerBase;
import org.xml.sax.SAXException;

public class cgGPXParser extends HandlerBase {
	ArrayList<cgCache> caches = null;

	public cgGPXParser(ArrayList<cgCache> cachesIn) {
		caches = cachesIn;
	}

	@Override
	public void startDocument() throws SAXException {
		// TODO: implement
	}

	@Override
	public void startElement(String name, AttributeList attrs) throws SAXException {
		// TODO: implement
	}

	@Override
	public void endElement(String name) throws SAXException {
		// TODO: implement
	}

	@Override
	public void characters(char[] chars, int offset, int len) throws SAXException {
		// TODO: implement
	}
}
