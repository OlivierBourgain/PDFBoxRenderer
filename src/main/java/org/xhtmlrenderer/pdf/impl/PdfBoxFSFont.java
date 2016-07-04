package org.xhtmlrenderer.pdf.impl;

import org.apache.pdfbox.pdmodel.font.PDFont;
import org.xhtmlrenderer.render.FSFont;

public class PdfBoxFSFont  implements FSFont {
	    private PDFont font;
	    private float size;
	    
	    public PdfBoxFSFont(PDFont font, float size) {
	        this.font = font;
	        this.size = size;
	    }

	    public float getSize2D() {
	        return size;
	    }
	    
	    public PDFont getFontDescription() {
	        return font;
	    }
}
