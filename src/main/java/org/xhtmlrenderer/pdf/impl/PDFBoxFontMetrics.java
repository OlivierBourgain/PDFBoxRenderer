package org.xhtmlrenderer.pdf.impl;

import org.xhtmlrenderer.render.FSFontMetrics;

public class PDFBoxFontMetrics implements FSFontMetrics {

	private float ascent;
	private float descent;
	private float strikethroughOffset;
	private float strikethroughThickness;
	private float underlineOffset;
	private float underlineThickness;

	@Override
	public float getAscent() {
		return ascent;
	}

	public void setAscent(float ascent) {
		this.ascent = ascent;
	}

	@Override
	public float getDescent() {
		return descent;
	}

	public void setDescent(float descent) {
		this.descent = descent;
	}

	@Override
	public float getStrikethroughOffset() {
		return strikethroughOffset;
	}

	public void setStrikethroughOffset(float strikethroughOffset) {
		this.strikethroughOffset = strikethroughOffset;
	}

	@Override
	public float getStrikethroughThickness() {
		return strikethroughThickness;
	}

	public void setStrikethroughThickness(float strikethroughThickness) {
		this.strikethroughThickness = strikethroughThickness;
	}

	@Override
	public float getUnderlineOffset() {
		return underlineOffset;
	}

	public void setUnderlineOffset(float underlineOffset) {
		this.underlineOffset = underlineOffset;
	}

	@Override
	public float getUnderlineThickness() {
		return underlineThickness;
	}

	public void setUnderlineThickness(float underlineThickness) {
		this.underlineThickness = underlineThickness;
	}

}
