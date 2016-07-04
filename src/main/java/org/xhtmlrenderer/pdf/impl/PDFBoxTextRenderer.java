package org.xhtmlrenderer.pdf.impl;

import java.awt.Rectangle;
import java.io.IOException;
import java.util.logging.Logger;

import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDFontDescriptor;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.xhtmlrenderer.extend.FSGlyphVector;
import org.xhtmlrenderer.extend.FontContext;
import org.xhtmlrenderer.extend.OutputDevice;
import org.xhtmlrenderer.extend.TextRenderer;
import org.xhtmlrenderer.render.FSFont;
import org.xhtmlrenderer.render.FSFontMetrics;
import org.xhtmlrenderer.render.JustificationInfo;

public class PDFBoxTextRenderer implements TextRenderer {
	private static Logger log = Logger.getLogger("PDFBoxTextRenderer");

	@Override
	public void setup(FontContext context) {
		log.info("Setup of the PDFBoxTextRenderer");
	}

	@Override
	public void drawString(OutputDevice outputDevice, String s, float x, float y) {
		((PDFBoxOutputDevice) outputDevice).drawString(s, x, y, null);
	}

	@Override
	public void drawString(OutputDevice outputDevice, String s, float x, float y, JustificationInfo info) {
		((PDFBoxOutputDevice) outputDevice).drawString(s, x, y, info);
	}

	@Override
	public void drawGlyphVector(OutputDevice outputDevice, FSGlyphVector vector, float x, float y) {
		throw new UnsupportedOperationException();
	}

	@Override
	public FSFontMetrics getFSFontMetrics(FontContext context, FSFont font, String string) {
		log.info("getFSFontMetrics");
		PdfBoxFSFont f = (PdfBoxFSFont) font;
		PDFontDescriptor fd = f.getFontDescription().getFontDescriptor();

		PDFBoxFontMetrics result = new PDFBoxFontMetrics();
		result.setAscent(fd.getAscent()* f.getSize2D() / 1000f);
		result.setDescent(fd.getDescent()* f.getSize2D() / 1000f);

		return result;

	}

	/**
	 * Return the size of the String s, in the font font, in point.
	 */
	@Override
	public int getWidth(FontContext context, FSFont font, String s) {
		if (s == null || s.length() == 0)
			return 0;
		PdfBoxFSFont f = (PdfBoxFSFont) font;
		try {
			// getStringWidth returns the width in 1/1000 units of text space.
			float result = f.getFontDescription().getStringWidth(s) * f.getSize2D() / 1000f;
			return (int) result;
		} catch (IOException e) {
			log.severe("Erreur getWidth");
			return 0;
		}
	}

	// Not implemented
	@Override
	public void setFontScale(float scale) {
	}

	@Override
	public float getFontScale() {
		return 0;
	}

	@Override
	public void setSmoothingThreshold(float fontsize) {
	}

	@Override
	public int getSmoothingLevel() {
		return 0;
	}

	@Override
	public void setSmoothingLevel(int level) {
	}

	@Override
	public FSGlyphVector getGlyphVector(OutputDevice outputDevice, FSFont font, String string) {
		throw new UnsupportedOperationException();
	}

	@Override
	public float[] getGlyphPositions(OutputDevice outputDevice, FSFont font, FSGlyphVector fsGlyphVector) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Rectangle getGlyphBounds(OutputDevice outputDevice, FSFont font, FSGlyphVector fsGlyphVector, int index, float x, float y) {
		throw new UnsupportedOperationException();
	}

}
