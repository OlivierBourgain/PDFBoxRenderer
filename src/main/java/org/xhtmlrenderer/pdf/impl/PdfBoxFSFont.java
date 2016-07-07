package org.xhtmlrenderer.pdf.impl;

import org.xhtmlrenderer.pdf.impl.PdfBoxFontResolver.FontDescription;
import org.xhtmlrenderer.render.FSFont;

public class PdfBoxFSFont  implements FSFont {
	    private FontDescription font;
	    private float size;
	    
	    public PdfBoxFSFont(FontDescription font, float size) {
	        this.font = font;
	        this.size = size;
	    }

	    public float getSize2D() {
	        return size;
	    }
	    
	    public FontDescription getFontDescription() {
	        return font;
	    }
}
