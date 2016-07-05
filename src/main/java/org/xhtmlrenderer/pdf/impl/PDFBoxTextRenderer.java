package org.xhtmlrenderer.pdf.impl;

import java.awt.Rectangle;
import java.io.IOException;
import java.util.logging.Logger;

import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDFontDescriptor;
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
		PDFont pdfont = f.getFontDescription().getFont();
		PDFontDescriptor fd = pdfont.getFontDescriptor();

		float size = f.getSize2D();

		PDFBoxFontMetrics result = new PDFBoxFontMetrics();
		result.setAscent(fd.getAscent() * size / 1000f);
		result.setDescent(fd.getDescent() * size / 1000f);

		result.setStrikethroughOffset(-f.getFontDescription().getYStrikeoutPosition() / 1000f * size);
		if (f.getFontDescription().getYStrikeoutSize() != 0) {
			result.setStrikethroughThickness(-f.getFontDescription().getYStrikeoutSize() / 1000f * size);
		} else {
			result.setStrikethroughThickness(size / 12.0f);
		}

		result.setUnderlineOffset(-f.getFontDescription().getUnderlinePosition() / 1000f * size);
		result.setUnderlineThickness(f.getFontDescription().getUnderlineThickness() / 1000f * size);

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
		PDFont pdfont = f.getFontDescription().getFont();
		try {
			// getStringWidth returns the width in 1/1000 units of text space.
			float result = pdfont.getStringWidth(s) * f.getSize2D() / 1000f;
			return (int) result;
		} catch (IllegalArgumentException e) {
			// Thrown when doc contains a non printable char
			log.info("IllegalArgumentException for |" + s + "|");
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
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
